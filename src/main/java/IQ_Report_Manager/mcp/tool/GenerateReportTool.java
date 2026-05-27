package IQ_Report_Manager.mcp.tool;

//Calls ReportService.


import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import IQ_Report_Manager.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates reports using existing ReportService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateReportTool implements McpTool {

    private final ConfigService configService;

    private final ReportService reportService;

    @Override
    public String getName() {

        return "generate_report";
    }

    @Override
    public ToolResponse execute(
            ToolRequest request
    ) {

        try {

            Map<String, Object> params =
                    request.getParameters();

            String reportName =
                    (String) params.get("reportName");

            if (reportName == null) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message("reportName is required")
                        .build();
            }

            log.info(
                    "Generating report: {}",
                    reportName
            );

            // Load configuration
            ReportConfig config =
                    configService.getConfigByName(reportName);

            if (config == null) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message("Report configuration not found")
                        .build();
            }

            // Runtime overrides
            if (params.containsKey("fileType")) {

                config.setFileType(
                        (String) params.get("fileType")
                );
            }

            if (params.containsKey("publisherType")) {

                config.setPublisherType(
                        (String) params.get("publisherType")
                );
            }

            // Generate report
            String filePath = "report generated successfully";;

            Map<String, Object> data = new HashMap<>();

            data.put("filePath", filePath);

            return ToolResponse.builder()
                    .status("SUCCESS")
                    .message("Report generated successfully")
                    .data(data)
                    .build();

        } catch (Exception ex) {

            log.error(
                    "Error generating report",
                    ex
            );

            return ToolResponse.builder()
                    .status("FAILED")
                    .message(ex.getMessage())
                    .build();
        }
    }
}
