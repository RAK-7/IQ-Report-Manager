package IQ_Report_Manager.mcp.dto;

import IQ_Report_Manager.mcp.tool.ToolSchema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class McpToolSchemaResponse {

    private String name;

    private String description;

    private List<ToolSchema> parameters;
}