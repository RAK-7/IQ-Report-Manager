package IQ_Report_Manager.mcp.transport;

import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.mcp.registry.ToolRegistry;
import IQ_Report_Manager.mcp.tool.McpTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class McpToolExecutor {

    private final ToolRegistry toolRegistry;

    public ToolResponse execute(
            String toolName,
            ToolRequest request
    ) {

        McpTool tool =
                toolRegistry.getTool(
                        toolName
                );

        if (tool == null) {

            return ToolResponse.builder()
                    .status("FAILED")
                    .message(
                            "Unknown tool: "
                                    + toolName
                    )
                    .build();
        }

        return tool.execute(
                request
        );
    }
}