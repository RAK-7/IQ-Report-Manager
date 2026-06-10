package IQ_Report_Manager.mcp.server;

import IQ_Report_Manager.mcp.dto.*;
import IQ_Report_Manager.mcp.registry.ToolRegistry;
import IQ_Report_Manager.mcp.tool.ToolSchema;
import IQ_Report_Manager.mcp.transport.McpToolExecutor;
import IQ_Report_Manager.mcp.tool.ToolMetadata;
import IQ_Report_Manager.mcp.tool.ToolSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class McpServerService {

    private final McpToolExecutor toolExecutor;

    private final ToolRegistry toolRegistry;

    public McpResponse execute(
            McpRequest request
    ) {

        ToolRequest toolRequest =
                ToolRequest.builder()
                        .toolName(
                                request.getTool()
                        )
                        .parameters(
                                request.getParameters()
                        )
                        .build();

        ToolResponse response =
                toolExecutor.execute(
                        request.getTool(),
                        toolRequest
                );

        return McpResponse.builder()
                .status(
                        response.getStatus()
                )
                .tool(
                        request.getTool()
                )
                .result(
                        response.getData()
                )
                .message(
                        response.getMessage()
                )
                .build();
    }
    public List<McpToolInfo> discoverTools() {

        return toolRegistry.getAllTools()
                .stream()
                .map(tool -> {

                    ToolMetadata metadata =
                            tool.getMetadata();

                    return McpToolInfo.builder()
                            .name(
                                    metadata.getToolName()
                            )
                            .description(
                                    metadata.getDescription()
                            )
                            .parameters(
                                    metadata.getParameters()
                                            .stream()
                                            .map(
                                                    ToolSchema::getName
                                            )
                                            .toList()
                            )
                            .build();
                })
                .toList();
    }

    public McpToolSchemaResponse getToolSchema(
            String toolName
    ) {

        ToolMetadata metadata =
                toolRegistry.getToolMetadata(
                        toolName
                );

        if (metadata == null) {

            throw new RuntimeException(
                    "Tool not found: "
                            + toolName
            );
        }

        return McpToolSchemaResponse
                .builder()
                .name(
                        metadata.getToolName()
                )
                .description(
                        metadata.getDescription()
                )
                .parameters(
                        metadata.getParameters()
                )
                .build();
    }
}