package IQ_Report_Manager.mcp.tool;

//Uses SchedulerService.


import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleReportTool implements McpTool {

    private final ConfigService configService;

    @Override
    public String getName() {
        return "schedule_report";
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        try {

            String reportName =
                    (String) request.getParameters()
                            .get("reportName");

            Long triggerTime =
                    Long.valueOf(
                            request.getParameters()
                                    .get("triggerTime")
                                    .toString()
                    );

            ReportConfig config =
                    configService.getConfigByName(reportName);

            if (config == null) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message("Report config not found")
                        .build();
            }

            config.setSchedulerType("TRIGGER");
            config.setTriggerTime(triggerTime);

            configService.saveConfig(config);

            return ToolResponse.builder()
                    .status("SUCCESS")
                    .message("Report scheduled successfully")
                    .build();

        } catch (Exception ex) {

            return ToolResponse.builder()
                    .status("FAILED")
                    .message(ex.getMessage())
                    .build();
        }
    }
}
