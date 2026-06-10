package IQ_Report_Manager.mcp.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single tool parameter definition.
 *
 * Example:
 *
 * reportName -> String -> required
 * fileType   -> String -> optional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolSchema {

    /**
     * Parameter name.
     *
     * Example:
     * reportName
     * fileType
     */
    private String name;

    /**
     * Parameter type.
     *
     * Example:
     * String
     * Long
     * Boolean
     */
    private String type;

    /**
     * Human readable description.
     */
    private String description;

    /**
     * Whether parameter is required.
     */
    private boolean required;
}