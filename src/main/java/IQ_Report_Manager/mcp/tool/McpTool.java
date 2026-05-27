package IQ_Report_Manager.mcp.tool;


import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;

/**
 * Base contract for all MCP tools.
 */
public interface McpTool {

    /**
     * Tool name.
     */
    String getName();

    /**
     * Executes tool logic.
     */
    ToolResponse execute(ToolRequest request);
}
