package IQ_Report_Manager.ai.dto;

//Result returned by a tool.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Tool execution response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallResult {

    /**
     * Tool name.
     */
    private String toolName;

    /**
     * SUCCESS / FAILED
     */
    private String status;

    /**
     * Human-readable response.
     */
    private String message;

    /**
     * Raw output from tool execution.
     */
    private Map<String, Object> output;
}
