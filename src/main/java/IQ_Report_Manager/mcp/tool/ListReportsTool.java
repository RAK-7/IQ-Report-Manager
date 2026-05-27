package IQ_Report_Manager.mcp.tool;

//Calls ConfigService.



import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lists all available reports.
 */
@Component
@RequiredArgsConstructor
public class ListReportsTool implements McpTool {

    private final ConfigService configService;

    @Override
    public String getName() {

        return "list_reports";
    }

    @Override
    public ToolResponse execute(
            ToolRequest request
    ) {

        List<ReportConfig> reports =
                configService.getAllConfigs();

        List<String> reportNames =
                reports.stream()
                        .map(ReportConfig::getName)
                        .collect(Collectors.toList());

        Map<String, Object> data =
                new HashMap<>();

        data.put("reports", reportNames);

        return ToolResponse.builder()
                .status("SUCCESS")
                .message("Available reports retrieved")
                .data(data)
                .build();
    }
}
