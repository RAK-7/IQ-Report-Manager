package IQ_Report_Manager.mcp.tool;

//Publishes an already generated file.


import IQ_Report_Manager.ai.executor.ReportExecutionResult;
import IQ_Report_Manager.factory.publisher.PublisherFactory;
import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.publisher.Publisher;
import IQ_Report_Manager.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublishReportTool implements McpTool {

    private final ConfigService configService;

    private final PublisherFactory publisherFactory;

    private ReportExecutionResult getExecutionResult(
            ToolRequest request
    ) {

        Object result =
                request.getParameters()
                        .get("executionResult");

        if (result instanceof ReportExecutionResult) {

            return (ReportExecutionResult) result;
        }

        return null;
    }

    @Override
    public String getName() {
        return "publish_report";
    }

    @Override
    public ToolMetadata getMetadata() {

        ToolMetadata metadata =
                ToolMetadata.builder()
                        .toolName("publish_report")
                        .description(
                                "Publish a generated report using configured publisher"
                        )
                        .build();

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportName")
                        .type("String")
                        .description(
                                "Report configuration name"
                        )
                        .required(true)
                        .build()
        );

        return metadata;
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        try {

            if (request.getParameters() == null) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message("Parameters are required")
                        .build();
            }

            /*
             * Get execution result
             * from previous tool.
             */
            ReportExecutionResult executionResult =
                    getExecutionResult(
                            request
                    );

            if (executionResult == null) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message(
                                "ReportExecutionResult missing"
                        )
                        .build();
            }

            /*
             * Config already available.
             */
            ReportConfig config =
                    executionResult.getReportConfig();

            if (config == null) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message(
                                "ReportConfig missing"
                        )
                        .build();
            }

            Publisher publisher =
                    publisherFactory.getPublisher(
                            config.getPublisher()
                    );

            publisher.publish(
                    config,
                    executionResult.getFileName()
            );

            return ToolResponse.builder()
                    .status("SUCCESS")
                    .message(
                            "Report published successfully"
                    )
                    .build();

        } catch (Exception ex) {

            return ToolResponse.builder()
                    .status("FAILED")
                    .message(ex.getMessage())
                    .build();
        }
    }
}
