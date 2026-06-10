package IQ_Report_Manager.ai.response;

import IQ_Report_Manager.ai.executor.ExecutionContext;
import IQ_Report_Manager.ai.executor.ReportExecutionResult;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import org.springframework.stereotype.Service;

@Service
public class AgentResponseGenerator {

    public String generateResponse(
            ExecutionContext context
    ) {

        StringBuilder response =
                new StringBuilder();

        /*
         * Report Config Found
         */
        ToolResponse configResponse =
                (ToolResponse) context.get(
                        "find_report_config"
                );

        if (configResponse != null
                && "SUCCESS".equals(
                configResponse.getStatus()
        )) {

            response.append(
                    "Report configuration found successfully.\n\n"
            );
        }

        /*
         * Report Generated
         */
        ToolResponse generateResponse =
                (ToolResponse) context.get(
                        "generate_report"
                );

        if (generateResponse != null
                && generateResponse.getData() != null) {

            Object executionResultObj =
                    generateResponse.getData()
                            .get(
                                    "executionResult"
                            );

            if (executionResultObj
                    instanceof ReportExecutionResult executionResult) {

                response.append(
                        "Report generated successfully.\n"
                );

                response.append(
                        "Report Name : "
                ).append(
                        executionResult.getReportName()
                ).append("\n");

                response.append(
                        "File Name : "
                ).append(
                        executionResult.getFileName()
                ).append("\n");

                response.append(
                        "File Type : "
                ).append(
                        executionResult.getFileType()
                ).append("\n\n");
            }
        }

        /*
         * Publish
         */
        ToolResponse publishResponse =
                (ToolResponse) context.get(
                        "publish_report"
                );

        if (publishResponse != null
                && "SUCCESS".equals(
                publishResponse.getStatus()
        )) {

            response.append(
                    "Report published successfully.\n\n"
            );
        }

        /*
         * Schedule
         */
        ToolResponse scheduleResponse =
                (ToolResponse) context.get(
                        "schedule_report"
                );

        if (scheduleResponse != null
                && "SUCCESS".equals(
                scheduleResponse.getStatus()
        )) {

            response.append(
                    "Report scheduled successfully.\n\n"
            );
        }

        return response.toString();
    }
}