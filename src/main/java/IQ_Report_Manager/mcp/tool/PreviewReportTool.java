package IQ_Report_Manager.mcp.tool;

//Returns sample data.

import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreviewReportTool implements McpTool {

    @Override
    public String getName() {
        return "preview_report";
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        return ToolResponse.builder()
                .status("SUCCESS")
                .message("Preview functionality coming next phase")
                .build();
    }
}
