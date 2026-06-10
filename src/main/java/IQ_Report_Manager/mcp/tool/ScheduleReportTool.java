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

    private Long calculateTriggerTime(
            String frequency
    ) {

        long now =
                System.currentTimeMillis();

        return switch (
                frequency.toLowerCase()
                ) {

            case "daily" ->
                    now + (24 * 60 * 60 * 1000L);

            case "weekly" ->
                    now + (7 * 24 * 60 * 60 * 1000L);

            case "monthly" ->
                    now + (30L * 24 * 60 * 60 * 1000);

            default ->
                    now + (60 * 60 * 1000L);
        };
    }

    @Override
    public String getName() {
        return "schedule_report";
    }

    @Override
    public ToolMetadata getMetadata() {

        ToolMetadata metadata =
                ToolMetadata.builder()
                        .toolName("schedule_report")
                        .description(
                                "Schedule future report execution"
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

        metadata.addParameter(
                ToolSchema.builder()
                        .name("triggerTime")
                        .type("Long")
                        .description(
                                "Unix timestamp in milliseconds"
                        )
                        .required(true)
                        .build()
        );

        return metadata;
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        try {

            String reportName =
                    (String) request.getParameters()
                            .get("reportName");

            Long triggerTime;

            if (request.getParameters()
                    .containsKey(
                            "triggerTime"
                    )) {

                triggerTime =
                        Long.valueOf(
                                request.getParameters()
                                        .get("triggerTime")
                                        .toString()
                        );
            }
            else {

                String frequency =
                        request.getParameters()
                                .getOrDefault(
                                        "frequency",
                                        "daily"
                                )
                                .toString();

                triggerTime =
                        calculateTriggerTime(
                                frequency
                        );
            }

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

            if (request.getParameters()
                    .containsKey("frequency")) {

                config.setFrequency(
                        request.getParameters()
                                .get("frequency")
                                .toString()
                );
            }

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
