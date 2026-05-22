package IQ_Report_Manager.ai.dto;

//Request to invoke a tool.
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a tool execution request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallRequest {

    /**
     * Tool name.
     */
    private String toolName;

    /**
     * Tool input parameters.
     */
    private Map<String, Object> parameters;
}
