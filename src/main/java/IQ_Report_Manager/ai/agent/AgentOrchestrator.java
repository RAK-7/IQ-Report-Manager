package IQ_Report_Manager.ai.agent;

import IQ_Report_Manager.ai.dto.AgentRequest;
import IQ_Report_Manager.ai.dto.AgentResponse;
import IQ_Report_Manager.ai.executor.*;
import IQ_Report_Manager.ai.memory.MemoryContext;
import IQ_Report_Manager.ai.memory.MemoryResolver;
import IQ_Report_Manager.ai.memory.service.ConversationMemoryService;
import IQ_Report_Manager.ai.planner.ExecutionPlan;
import IQ_Report_Manager.ai.planner.ExecutionPlanner;
import IQ_Report_Manager.ai.planner.RecoveryPlanner;
import IQ_Report_Manager.ai.response.AgentResponseGenerator;
import IQ_Report_Manager.ai.response.ResponseFormatter;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final ConversationMemoryService memoryService;

    private final ResponseFormatter responseFormatter;

    private final ExecutionPlanner executionPlanner;

    private final PlanExecutor planExecutor;

    private final AgentResponseGenerator responseGenerator;

    private final MemoryResolver memoryResolver;

    private final RecoveryPlanner recoveryPlanner;

    public AgentResponse process(AgentRequest request) {

        // Declare outside try so catch can access it for the response
        String conversationId =
                request.getConversationId() != null
                        ? request.getConversationId()
                        : UUID.randomUUID().toString();

        try {

            /*
             * Load memory.
             */
            MemoryContext memoryContext =
                    memoryService.getOrCreateContext(
                            conversationId,
                            request.getUserId()
                    );

            /*
             * Save current intent.
             */
            memoryContext.setLastIntent(
                    request.getMessage()
            );

            memoryService.updateContext(
                    memoryContext,
                    request.getMessage()
            );

            log.info(
                    "Processing conversation {}",
                    conversationId
            );

            /*
             * Create plan.
             */
            String resolvedRequest =
                    memoryResolver.resolve(
                            request.getMessage(),
                            memoryContext
                    );

            ExecutionPlan plan =
                    executionPlanner.createPlan(
                            resolvedRequest,
                            memoryContext
                    );

            log.info(
                    "Execution plan created with {} steps",
                    plan.getSteps().size()
            );

            /*
             * Execute the plan (THIS WAS THE MISSING CALL).
             */
            ExecutionResult executionResult =
                    planExecutor.execute(
                            plan,
                            conversationId
                    );

            /*
             * If execution failed, attempt recovery.
             */
            if (!executionResult.isSuccess()) {

                log.warn(
                        "Execution failed at tool '{}'. Attempting recovery.",
                        executionResult.getFailedTool()
                );

                FailureAnalysis failure =
                        FailureAnalysis.builder()
                                .failedTool(
                                        executionResult.getFailedTool()
                                )
                                .errorMessage(
                                        executionResult.getErrorMessage()
                                )
                                .build();

                ExecutionPlan recoveryPlan =
                        recoveryPlanner.buildRecoveryPlan(
                                failure
                        );

                if (recoveryPlan.getSteps() != null
                        && !recoveryPlan.getSteps().isEmpty()) {

                    executionResult =
                            planExecutor.execute(
                                    recoveryPlan,
                                    conversationId
                            );
                }
            }

            /*
             * If still failed after recovery, return failure response.
             */
            if (!executionResult.isSuccess()) {

                log.warn(
                        "Execution still failed after recovery at tool '{}'",
                        executionResult.getFailedTool()
                );

                return responseFormatter.failure(
                        executionResult.getErrorMessage(),
                        conversationId
                );
            }

            ExecutionContext executionContext =
                    executionResult.getContext();

            /*
             * Save report metadata into memory.
             */
            Object generateResponse =
                    executionContext.get(
                            "generate_report"
                    );

            if (generateResponse instanceof ToolResponse toolResponse
                    && toolResponse.getData() != null) {

                Object result =
                        toolResponse.getData()
                                .get(
                                        "executionResult"
                                );

                if (result instanceof ReportExecutionResult reportResult) {

                    memoryService.saveLastReport(
                            conversationId,
                            reportResult.getReportName()
                    );

                    memoryService.saveExecutionResult(
                            conversationId,
                            reportResult
                    );
                }
            }

            /*
             * Save plan to memory.
             */
            memoryService.updateContext(
                    memoryContext,
                    "Executed plan: " + plan.getSteps().size() + " steps"
            );

            /*
             * Generate natural language response.
             */
            String finalResponse =
                    responseGenerator.generateResponse(
                            executionContext
                    );

            return responseFormatter.success(
                    finalResponse,
                    plan,
                    conversationId
            );

        } catch (Exception ex) {

            log.error(
                    "Agent execution failed",
                    ex
            );

            return responseFormatter.failure(
                    ex.getMessage(),
                    conversationId
            );
        }
    }
}