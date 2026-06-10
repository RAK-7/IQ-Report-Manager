package IQ_Report_Manager.mcp.dto;

import lombok.Data;

import java.util.Map;

/**
 * Generic MCP request.
 */
@Data
public class McpRequest {

    /**
     * Tool name.
     */
    private String tool;

    /**
     * Tool parameters.
     */
    private Map<String,Object> parameters;
}