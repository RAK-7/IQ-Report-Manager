package IQ_Report_Manager.ai.executor;

import IQ_Report_Manager.ai.audit.ExecutionAuditService;
import IQ_Report_Manager.ai.planner.ExecutionPlan;
import IQ_Report_Manager.ai.planner.PlanStep;
import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.mcp.registry.ToolRegistry;
import IQ_Report_Manager.mcp.tool.McpTool;
import IQ_Report_Manager.ai.audit.ExecutionAuditService;
import IQ_Report_Manager.ai.metrics.AgentMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class PlanExecutor {

    private final ToolRegistry toolRegistry;

    private final ExecutionAuditService auditService;

    private final AgentMetricsService metricsService;

    public ExecutionResult execute(
            ExecutionPlan plan,
            String conversationId
    ) {

        ExecutionContext context =
                new ExecutionContext();

        context.setConversationId(
                conversationId
        );

        for (PlanStep step : plan.getSteps()) {

            /*
             * Inject outputs from previous tools.
             */
            injectContextValues(
                    step,
                    context
            );

            McpTool tool =
                    toolRegistry.getTool(
                            step.getTool()
                    );

            if (tool == null) {

                auditService.saveAudit(
                        conversationId,
                        step.getTool(),
                        step.getParameters(),
                        "FAILED",
                        "Tool not found",
                        0L
                );

                throw new RuntimeException(
                        "Tool not found: "
                                + step.getTool()
                );
            }

            ToolRequest request =
                    ToolRequest.builder()
                            .toolName(
                                    step.getTool()
                            )
                            .parameters(
                                    step.getParameters()
                            )
                            .build();

            /*
             * Start timer.
             */
            long startTime =
                    System.currentTimeMillis();

            ToolResponse response =
                    tool.execute(
                            request
                    );

            /*
             * Execution duration.
             */
            long duration =
                    System.currentTimeMillis()
                            - startTime;

            /*
             * Failure audit.
             */
            if (!"SUCCESS".equalsIgnoreCase(
                    response.getStatus()
            )) {

                auditService.saveAudit(
                        conversationId,
                        step.getTool(),
                        step.getParameters(),
                        response.getStatus(),
                        response.getMessage(),
                        duration
                );

                return ExecutionResult.builder()
                        .success(false)
                        .failedTool(
                                step.getTool()
                        )
                        .errorMessage(
                                response.getMessage()
                        )
                        .retryCount(0)
                        .context(context)
                        .build();
            }

            /*
             * Success audit.
             */
            auditService.saveAudit(
                    conversationId,
                    step.getTool(),
                    step.getParameters(),
                    response.getStatus(),
                    null,
                    duration
            );

            /*
             * Save output into context.
             */
            context.put(
                    step.getTool(),
                    response
            );

            context.put(
                    step.getTool()
                            + "_"
                            + step.getOrder(),
                    response
            );
        }

        return ExecutionResult.builder()
                .success(true)
                .retryCount(0)
                .context(context)
                .build();
    }    /**
     * Uses outputs of previous tools
     * to enrich later tool requests.
     */
    private void injectContextValues(
            PlanStep step,
            ExecutionContext context
    ) {

        Object generateResponse =
                context.get(
                        "generate_report"
                );

        if (generateResponse != null) {

            ToolResponse response =
                    (ToolResponse) generateResponse;

            if (response.getData() != null) {

                Object executionResult =
                        response.getData()
                                .get(
                                        "executionResult"
                                );

                if (executionResult != null) {

                    if (step.getParameters() == null) {

                        step.setParameters(
                                new HashMap<>()
                        );
                    }

                    step.getParameters()
                            .put(
                                    "executionResult",
                                    executionResult
                            );
                }
            }
        }

        Object configResponse =
                context.get(
                        "find_report_config"
                );

        if (configResponse != null) {

            ToolResponse response =
                    (ToolResponse) configResponse;

            if (response.getData() != null) {

                Object reportName =
                        response.getData()
                                .get("reportName");

                if (reportName != null) {

                    if (step.getParameters() == null) {

                        step.setParameters(
                                new HashMap<>()
                        );
                    }

                    step.getParameters()
                            .putIfAbsent(
                                    "reportName",
                                    reportName
                            );
                }
            }
        }
    }
}