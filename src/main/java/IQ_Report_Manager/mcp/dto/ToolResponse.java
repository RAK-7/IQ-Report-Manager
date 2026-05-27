package IQ_Report_Manager.mcp.dto;

//Standard output payload.


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Generic MCP tool response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResponse {

    /**
     * SUCCESS / FAILED
     */
    private String status;

    /**
     * Human-readable response.
     */
    private String message;

    /**
     * Tool output.
     */
    private Map<String, Object> data;
}
