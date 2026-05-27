package IQ_Report_Manager.mcp.dto;

//Standard input payload for tools.


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Generic MCP tool request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolRequest {

    /**
     * Tool name.
     */
    private String toolName;

    /**
     * Tool parameters.
     */
    private Map<String, Object> parameters;
}
