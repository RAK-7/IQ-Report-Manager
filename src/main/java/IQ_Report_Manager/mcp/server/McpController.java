package IQ_Report_Manager.mcp.server;

import IQ_Report_Manager.mcp.dto.McpRequest;
import IQ_Report_Manager.mcp.dto.McpResponse;
import IQ_Report_Manager.mcp.dto.McpToolInfo;
import IQ_Report_Manager.mcp.dto.McpToolSchemaResponse;
import IQ_Report_Manager.mcp.tool.ToolMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpServerService serverService;

    @PostMapping("/execute")
    public McpResponse execute(
            @RequestBody McpRequest request
    ) {

        return serverService.execute(
                request
        );
    }
    @GetMapping("/tools")
    public List<McpToolInfo> tools() {

        return serverService.discoverTools();
    }

    @GetMapping("/tools/{toolName}")
    public McpToolSchemaResponse getToolSchema(
            @PathVariable
            String toolName
    ) {

        return serverService.getToolSchema(
                toolName
        );
    }
}