package IQ_Report_Manager.mcp.registry;

//Stores and resolves available tools.


import IQ_Report_Manager.mcp.tool.McpTool;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central MCP tool registry.
 */
@Component
@RequiredArgsConstructor
public class ToolRegistry {

    /**
     * All Spring-managed tools.
     */
    private final List<McpTool> tools;

    /**
     * Fast lookup map.
     */
    private final Map<String, McpTool> toolMap =
            new HashMap<>();

    @PostConstruct
    public void init() {

        for (McpTool tool : tools) {

            toolMap.put(
                    tool.getName(),
                    tool
            );
        }
    }

    /**
     * Returns tool by name.
     */
    public McpTool getTool(String name) {

        return toolMap.get(name);
    }
}
