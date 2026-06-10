package IQ_Report_Manager.mcp.tool;

//Validates configuration correctness.

import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidateReportConfigTool implements McpTool {

    private final ConfigService configService;

    @Override
    public String getName() {
        return "validate_report";
    }

    @Override
    public ToolMetadata getMetadata() {

        ToolMetadata metadata =
                ToolMetadata.builder()
                        .toolName("validate_report")
                        .description(
                                "Validate report configuration"
                        )
                        .build();

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportName")
                        .type("String")
                        .description(
                                "Report configuration name"
                        )
                        .required(true)
                        .build()
        );

        return metadata;
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        String reportName =
                (String) request.getParameters()
                        .get("reportName");

        ReportConfig config =
                configService.getConfigByName(reportName);

        if (config == null) {

            return ToolResponse.builder()
                    .status("FAILED")
                    .message("Report config not found")
                    .build();
        }

        if (config.getDbType() == null)
            return fail("dbType missing");

        if (config.getReportType() == null)
            return fail("reportType missing");

        if (config.getFileType() == null)
            return fail("fileType missing");

        return ToolResponse.builder()
                .status("SUCCESS")
                .message("Configuration valid")
                .build();
    }

    private ToolResponse fail(String msg) {

        return ToolResponse.builder()
                .status("FAILED")
                .message(msg)
                .build();
    }
}
