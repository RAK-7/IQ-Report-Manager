package IQ_Report_Manager.mcp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class McpResponse {

    private String status;

    private String tool;

    private Object result;

    private String message;
}