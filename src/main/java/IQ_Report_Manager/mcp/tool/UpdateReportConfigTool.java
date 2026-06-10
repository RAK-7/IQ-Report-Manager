package IQ_Report_Manager.mcp.tool;

//Modifies existing configurations.

import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateReportConfigTool implements McpTool {

    private final ConfigService configService;

    @Override
    public String getName() {
        return "update_report_config";
    }

    @Override
    public ToolMetadata getMetadata() {

        ToolMetadata metadata =
                ToolMetadata.builder()
                        .toolName("update_report_config")
                        .description("Update an existing report configuration")
                        .build();

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportName")
                        .type("String")
                        .description("Existing report name")
                        .required(true)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("fileType")
                        .type("String")
                        .description("CSV or XLSX")
                        .required(false)
                        .build()
        );

        return metadata;
    }

    @Override
    public ToolResponse execute(
            ToolRequest request
    ) {

        try {

            String reportName =
                    (String) request.getParameters()
                            .get("reportName");

            ReportConfig config =
                    configService.getConfigByName(
                            reportName
                    );

            if (config == null) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message("Config not found")
                        .build();
            }

            if (request.getParameters()
                    .containsKey("fileType")) {

                config.setFileType(
                        (String) request.getParameters()
                                .get("fileType")
                );
            }

            configService.saveConfig(config);

            return ToolResponse.builder()
                    .status("SUCCESS")
                    .message("Configuration updated")
                    .build();

        } catch (Exception ex) {

            return ToolResponse.builder()
                    .status("FAILED")
                    .message(ex.getMessage())
                    .build();
        }
    }
}