package IQ_Report_Manager.ai.agent;

/**
Coordinates planner, memory, tool execution, and response formatting.
THIS IS THE HEART OF AGENT SYSTEM.
 */

import IQ_Report_Manager.ai.dto.AgentRequest;
import IQ_Report_Manager.ai.dto.AgentResponse;
import IQ_Report_Manager.ai.executor.PlanExecutor;
import IQ_Report_Manager.ai.memory.ConversationMemoryService;
import IQ_Report_Manager.ai.memory.MemoryContext;
import IQ_Report_Manager.ai.planner.ExecutionPlanner;
import IQ_Report_Manager.ai.response.ResponseFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.mcp.registry.ToolRegistry;
import IQ_Report_Manager.mcp.tool.McpTool;
import IQ_Report_Manager.ai.dto.ToolSelectionResponse;
import IQ_Report_Manager.ai.executor.ExecutionContext;
import IQ_Report_Manager.ai.planner.ExecutionPlan;

import java.util.UUID;

/**
 * Central orchestration engine.
 * Responsibilities:
 * - memory management
 * - planning
 * - execution coordination
 * - response generation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final ConversationMemoryService memoryService;

    private final ResponseFormatter responseFormatter;

    private final ExecutionPlanner executionPlanner;

    private final PlanExecutor planExecutor;

    /**
     * Main orchestration entry point.
     */
    public AgentResponse process(
            AgentRequest request
    ) {

        try {

            String conversationId =
                    request.getConversationId() != null
                            ? request.getConversationId()
                            : UUID.randomUUID().toString();

            // Retrieve conversation memory
            MemoryContext memoryContext =
                    memoryService.getOrCreateContext(
                            conversationId,
                            request.getUserId()
                    );

            // Store incoming message
            memoryService.updateContext(
                    memoryContext,
                    request.getMessage()
            );

            log.info(
                    "Processing request for conversation: {}",
                    conversationId
            );

            /**
             * Step 1:
             * Convert user request into executable plan.
             */
            ExecutionPlan plan =
                    executionPlanner.createPlan(
                            request.getMessage()
                    );

            log.info(
                    "Execution plan created. Steps={}",
                    plan.getSteps().size()
            );

            /**
             * Step 2:
             * Execute plan.
             */
            ExecutionContext executionContext =
                    planExecutor.execute(
                            plan,
                            conversationId
                    );

            /**
             * Step 3:
             * Save plan into conversation memory.
             */
            memoryService.updateContext(
                    memoryContext,
                    plan.toString()
            );

            /**
             * Step 4:
             * Return response.
             */
            return responseFormatter.success(
                    "Execution completed successfully",
                    "Plan ID : " + plan.getPlanId()
            );

        } catch (Exception ex) {

            log.error(
                    "Error during orchestration",
                    ex
            );

            return responseFormatter.failure(
                    ex.getMessage()
            );
        }
    }

}
