package IQ_Report_Manager.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Structured AI tool selection response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolSelectionResponse {

    /**
     * Selected MCP tool.
     */
    private String tool;

    /**
     * Extracted parameters.
     */
    private Map<String, Object> parameters;
}