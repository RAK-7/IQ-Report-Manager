package IQ_Report_Manager.mcp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class McpToolInfo {

    private String name;

    private String description;

    private List<String> parameters;
}