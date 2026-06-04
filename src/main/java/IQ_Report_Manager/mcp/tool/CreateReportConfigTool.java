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
    public ToolResponse execute(ToolRequest request) {

        ReportConfig config =
                new ReportConfig();

        config.setName(
                (String) request.getParameters()
                        .get("name")
        );

        configService.saveConfig(config);

        return ToolResponse.builder()
                .status("SUCCESS")
                .message("Config created")
                .build();
    }
}
