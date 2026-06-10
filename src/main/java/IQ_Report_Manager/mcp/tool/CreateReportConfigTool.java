package IQ_Report_Manager.mcp.tool;

//Creates a new configuration.

import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateReportConfigTool implements McpTool {

    private final ConfigService configService;

    @Override
    public String getName() {
        return "create_report_config";
    }

    @Override
    public ToolMetadata getMetadata() {

        ToolMetadata metadata =
                ToolMetadata.builder()
                        .toolName("create_report_config")
                        .description("Create a new report configuration")
                        .build();

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportName")
                        .type("String")
                        .description("Name of report")
                        .required(true)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("dbType")
                        .type("String")
                        .description("MYSQL or ELASTICSEARCH")
                        .required(true)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportType")
                        .type("String")
                        .description("RAW or AGG")
                        .required(true)
                        .build()
        );

        return metadata;
    }

    @Override
    public ToolResponse execute(
            ToolRequest request
    ) {

        try {

            ReportConfig config =
                    new ReportConfig();

            config.setReportName(
                    (String) request.getParameters()
                            .get("reportName")
            );

            config.setDbType(
                    (String) request.getParameters()
                            .get("dbType")
            );

            config.setReportType(
                    (String) request.getParameters()
                            .get("reportType")
            );

            configService.saveConfig(config);

            return ToolResponse.builder()
                    .status("SUCCESS")
                    .message("Report configuration created")
                    .build();

        } catch (Exception ex) {

            return ToolResponse.builder()
                    .status("FAILED")
                    .message(ex.getMessage())
                    .build();
        }
    }
}
