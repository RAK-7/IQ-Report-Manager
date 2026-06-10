package IQ_Report_Manager.mcp.tool;

//returns sample data

import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PreviewReportTool implements McpTool {

    private final ConfigService configService;

    @Override
    public String getName() {
        return "preview_report";
    }

    @Override
    public ToolMetadata getMetadata() {

        ToolMetadata metadata =
                ToolMetadata.builder()
                        .toolName("preview_report")
                        .description("Preview report configuration")
                        .build();

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportName")
                        .type("String")
                        .description("Report configuration name")
                        .required(true)
                        .build()
        );

        return metadata;
    }

    @Override
    public ToolResponse execute(
            ToolRequest request
    ) {

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
                    .message("Report config not found")
                    .build();
        }

        Map<String,Object> data =
                new HashMap<>();

        data.put("config", config);

        return ToolResponse.builder()
                .status("SUCCESS")
                .message("Preview generated")
                .data(data)
                .build();
    }
}