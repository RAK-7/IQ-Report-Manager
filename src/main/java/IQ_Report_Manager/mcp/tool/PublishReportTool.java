package IQ_Report_Manager.mcp.tool;

//Publishes an already generated file.


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

    @Override
    public String getName() {
        return "publish_report";
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        try {

            String reportName =
                    (String) request.getParameters()
                            .get("reportName");

            String fileName =
                    (String) request.getParameters()
                            .get("fileName");

            ReportConfig config =
                    configService.getConfigByName(reportName);

            Publisher publisher =
                    publisherFactory.getPublisher(
                            config.getPublisher()
                    );

            publisher.publish(config, fileName);

            return ToolResponse.builder()
                    .status("SUCCESS")
                    .message("Report published")
                    .build();

        } catch (Exception ex) {

            return ToolResponse.builder()
                    .status("FAILED")
                    .message(ex.getMessage())
                    .build();
        }
    }
}
