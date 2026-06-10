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
     * Tool metadata.
     */
    ToolMetadata getMetadata();

    /**
     * Executes tool logic.
     */
    ToolResponse execute(ToolRequest request);
}
