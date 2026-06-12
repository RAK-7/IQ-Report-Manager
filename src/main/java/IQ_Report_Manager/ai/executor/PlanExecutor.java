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
             * Failure handling.
             *
             * Special case: find_report_config returns NOT_FOUND when no config exists.
             * This is NOT a fatal failure — the plan will continue to create_report_config.
             * Any other FAILED status stops execution immediately.
             */
            if (!("SUCCESS".equalsIgnoreCase(response.getStatus())
                    || "NOT_FOUND".equalsIgnoreCase(response.getStatus()))) {

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
    }

    /**
     * Uses outputs of previous tools
     * to enrich later tool requests.
     *
     * Injects:
     * - reportName from create_report_config → into all subsequent steps
     * - reportName from find_report_config   → into generate_report, publish_report, schedule_report
     * - executionResult from generate_report → into publish_report
     */
    private void injectContextValues(
            PlanStep step,
            ExecutionContext context
    ) {

        if (step.getParameters() == null) {
            step.setParameters(new HashMap<>());
        }

        /*
         * Inject reportName from create_report_config output.
         * This runs first when no config exists, so subsequent
         * steps (find_report_config, generate_report, publish_report)
         * get the correct reportName automatically.
         */
        Object createResponse = context.get("create_report_config");

        if (createResponse instanceof ToolResponse createToolResponse
                && createToolResponse.getData() != null) {

            Object reportName = createToolResponse.getData().get("reportName");

            if (reportName != null) {
                Object existing = step.getParameters().get("reportName");
                if (existing == null || existing.toString().isBlank()) {
                    step.getParameters().put("reportName", reportName);
                }
            }
        }

        /*
         * Inject reportName from find_report_config output.
         * Takes priority over create_report_config if both ran.
         */
        Object configResponse = context.get("find_report_config");

        if (configResponse instanceof ToolResponse configToolResponse
                && configToolResponse.getData() != null) {

            Object reportName = configToolResponse.getData().get("reportName");

            if (reportName != null) {
                Object existing = step.getParameters().get("reportName");
                if (existing == null || existing.toString().isBlank()) {
                    step.getParameters().put("reportName", reportName);
                }
            }
        }

        /*
         * Inject executionResult from generate_report output.
         * Needed by publish_report to know which file to attach.
         */
        Object generateResponse = context.get("generate_report");

        if (generateResponse instanceof ToolResponse generateToolResponse
                && generateToolResponse.getData() != null) {

            Object executionResult = generateToolResponse.getData().get("executionResult");

            if (executionResult != null) {
                step.getParameters().putIfAbsent("executionResult", executionResult);
            }
        }
    }
}