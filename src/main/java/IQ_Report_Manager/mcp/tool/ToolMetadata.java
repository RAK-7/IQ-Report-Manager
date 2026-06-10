package IQ_Report_Manager.mcp.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes an MCP Tool.
 *
 * Used for:
 * - Prompt generation
 * - Tool discovery
 * - AI planning
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolMetadata {

    /**
     * MCP Tool name.
     *
     * Example:
     * generate_report
     */
    private String toolName;

    /**
     * Tool description.
     */
    private String description;

    /**
     * Tool parameter definitions.
     */
    @Builder.Default
    private List<ToolSchema> parameters =
            new ArrayList<>();

    /**
     * Adds parameter definition.
     */
    public void addParameter(
            ToolSchema schema
    ) {

        if (parameters == null) {
            parameters = new ArrayList<>();
        }

        parameters.add(schema);
    }
}