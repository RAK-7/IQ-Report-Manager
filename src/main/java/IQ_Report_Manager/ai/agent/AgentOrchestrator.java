package IQ_Report_Manager.ai.agent;

/**
Coordinates planner, memory, tool execution, and response formatting.
THIS IS THE HEART OF AGENT SYSTEM.
 */

import IQ_Report_Manager.ai.dto.AgentRequest;
import IQ_Report_Manager.ai.dto.AgentResponse;
import IQ_Report_Manager.ai.memory.ConversationMemoryService;
import IQ_Report_Manager.ai.memory.MemoryContext;
import IQ_Report_Manager.ai.planner.AgentPlanner;
import IQ_Report_Manager.ai.response.ResponseFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.mcp.registry.ToolRegistry;
import IQ_Report_Manager.mcp.tool.McpTool;

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

    private final AgentPlanner agentPlanner;

    private final ResponseFormatter responseFormatter;

    private final ToolRegistry toolRegistry;

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

            // Generate execution plan
            String executionPlan =
                    agentPlanner.createPlan(
                            request.getMessage()
                    );

            ToolRequest toolRequest =
                    ToolRequest.builder()
                            .toolName("list_reports")
                            .build();

            ToolResponse toolResponse =
                    executeTool(
                            "list_reports",
                            toolRequest
                    );

            // Store generated plan
            memoryService.updateContext(
                    memoryContext,
                    executionPlan
            );

            return responseFormatter.success(
                    "Execution plan generated successfully.",
                    executionPlan
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
    private ToolResponse executeTool(
            String toolName,
            ToolRequest request
    ) {

        McpTool tool =
                toolRegistry.getTool(toolName);

        if (tool == null) {

            return ToolResponse.builder()
                    .status("FAILED")
                    .message(
                            "Tool not found: " + toolName
                    )
                    .build();
        }

        return tool.execute(request);
    }
}
