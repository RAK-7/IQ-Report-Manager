package IQ_Report_Manager.ai.response;

import IQ_Report_Manager.ai.executor.ExecutionContext;
import IQ_Report_Manager.ai.executor.ReportExecutionResult;
import IQ_Report_Manager.ai.llm.LlmService;
import IQ_Report_Manager.ai.prompt.PromptTemplate;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Generates a friendly natural language response
 * from the execution context using the LLM.
 *
 * Flow:
 *   ExecutionContext (tool outputs)
 *       ↓
 *   Build execution summary
 *       ↓
 *   LLM (Ollama) → natural language response
 *       ↓
 *   Return friendly message to user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentResponseGenerator {

    private final LlmService llmService;

    public String generateResponse(
            ExecutionContext context
    ) {

        /*
         * 1. Build a structured summary of what happened.
         */
        StringBuilder summary = new StringBuilder();
        summary.append("Execution Summary:\n\n");

        /*
         * Report Config Found
         */
        ToolResponse configResponse =
                (ToolResponse) context.get("find_report_config");

        if (configResponse != null
                && "SUCCESS".equals(configResponse.getStatus())) {

            String reportName = configResponse.getData() != null
                    ? String.valueOf(configResponse.getData().get("reportName"))
                    : "unknown";

            summary.append("- Report configuration found: ").append(reportName).append("\n");
        }

        /*
         * Report Config Created
         */
        ToolResponse createResponse =
                (ToolResponse) context.get("create_report_config");

        if (createResponse != null
                && "SUCCESS".equals(createResponse.getStatus())) {

            String reportName = createResponse.getData() != null
                    ? String.valueOf(createResponse.getData().get("reportName"))
                    : "unknown";

            summary.append("- New report configuration created: ").append(reportName).append("\n");
        }

        /*
         * Report Generated
         */
        ToolResponse generateResponse =
                (ToolResponse) context.get("generate_report");

        if (generateResponse != null
                && generateResponse.getData() != null) {

            Object executionResultObj =
                    generateResponse.getData().get("executionResult");

            if (executionResultObj instanceof ReportExecutionResult executionResult) {

                summary.append("- Report generated successfully.\n");
                summary.append("  Report Name  : ").append(executionResult.getReportName()).append("\n");
                summary.append("  File Name    : ").append(executionResult.getFileName()).append("\n");
                summary.append("  File Type    : ").append(executionResult.getFileType()).append("\n");
                summary.append("  Report Type  : ").append(executionResult.getReportType()).append("\n");
                summary.append("  Database     : ").append(executionResult.getDbType()).append("\n");
                summary.append("  Record Count : ").append(executionResult.getRecordCount()).append("\n");

                if (executionResult.getReportConfig() != null
                        && executionResult.getReportConfig().getEmail() != null) {
                    summary.append("  Email To     : ")
                            .append(executionResult.getReportConfig().getEmail())
                            .append("\n");
                }
            }
        }

        /*
         * Published
         */
        ToolResponse publishResponse =
                (ToolResponse) context.get("publish_report");

        if (publishResponse != null
                && "SUCCESS".equals(publishResponse.getStatus())) {

            summary.append("- Report published (emailed) successfully.\n");
        }

        /*
         * Scheduled
         */
        ToolResponse scheduleResponse =
                (ToolResponse) context.get("schedule_report");

        if (scheduleResponse != null
                && "SUCCESS".equals(scheduleResponse.getStatus())) {

            summary.append("- Report has been scheduled successfully.\n");
        }

        /*
         * List Reports
         */
        ToolResponse listResponse =
                (ToolResponse) context.get("list_reports");

        if (listResponse != null
                && "SUCCESS".equals(listResponse.getStatus())) {

            if (listResponse.getData() != null) {
                summary.append("- Available reports: ")
                        .append(listResponse.getData().get("reports"))
                        .append("\n");
            }
        }

        String summaryText = summary.toString();

        log.debug("Execution summary for NL response:\n{}", summaryText);

        /*
         * 2. Use the LLM to generate a friendly natural language response.
         */
        try {

            String nlResponse = llmService.generate(
                    PromptTemplate.NL_RESPONSE_PROMPT,
                    summaryText
            );

            if (nlResponse != null && !nlResponse.isBlank()) {
                log.info("LLM generated natural language response");
                return nlResponse.trim();
            }

        } catch (Exception ex) {

            log.warn(
                    "LLM response generation failed, falling back to template response: {}",
                    ex.getMessage()
            );
        }

        /*
         * 3. Fallback: return the structured summary if LLM fails.
         */
        return buildFallbackResponse(context);
    }

    /**
     * Template-based fallback response when LLM is unavailable.
     */
    private String buildFallbackResponse(ExecutionContext context) {

        StringBuilder response = new StringBuilder();

        ToolResponse createResponse = (ToolResponse) context.get("create_report_config");
        if (createResponse != null && "SUCCESS".equals(createResponse.getStatus())) {
            response.append("A new report configuration was created successfully. ");
        }

        ToolResponse configResponse = (ToolResponse) context.get("find_report_config");
        if (configResponse != null && "SUCCESS".equals(configResponse.getStatus())) {
            response.append("Found the matching report configuration. ");
        }

        ToolResponse generateResponse = (ToolResponse) context.get("generate_report");
        if (generateResponse != null && generateResponse.getData() != null) {
            Object resultObj = generateResponse.getData().get("executionResult");
            if (resultObj instanceof ReportExecutionResult result) {
                response.append("Your report '")
                        .append(result.getReportName())
                        .append("' has been generated successfully as a ")
                        .append(result.getFileType())
                        .append(" file with ")
                        .append(result.getRecordCount())
                        .append(" records. ");
            }
        }

        ToolResponse publishResponse = (ToolResponse) context.get("publish_report");
        if (publishResponse != null && "SUCCESS".equals(publishResponse.getStatus())) {
            response.append("The report has been emailed to the configured recipients. ");
        }

        ToolResponse scheduleResponse = (ToolResponse) context.get("schedule_report");
        if (scheduleResponse != null && "SUCCESS".equals(scheduleResponse.getStatus())) {
            response.append("The report has been scheduled and will run automatically as configured. ");
        }

        if (response.length() == 0) {
            response.append("Your request has been processed successfully.");
        }

        return response.toString().trim();
    }
}