package IQ_Report_Manager.mcp.tool;

//Schedules a report and registers the cron schedule immediately.

import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import IQ_Report_Manager.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * MCP Tool: Schedule a report for recurring execution.
 *
 * Supports:
 * - frequency: "daily", "weekly", "monthly"
 * - cron: explicit cron expression e.g. "0 8 * * *"
 * - triggerTime: specific Unix timestamp (ms)
 *
 * After saving the schedule to MongoDB, this tool immediately
 * registers it with SchedulerService so it starts running
 * without a server restart.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleReportTool implements McpTool {

    private final ConfigService configService;
    private final SchedulerService schedulerService;

    @Override
    public String getName() {
        return "schedule_report";
    }

    @Override
    public ToolMetadata getMetadata() {

        ToolMetadata metadata =
                ToolMetadata.builder()
                        .toolName("schedule_report")
                        .description("Schedule a report for recurring automatic execution")
                        .build();

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportName")
                        .type("String")
                        .description("Report configuration name to schedule")
                        .required(true)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("frequency")
                        .type("String")
                        .description("Schedule frequency: daily, weekly, or monthly")
                        .required(false)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("cron")
                        .type("String")
                        .description("Cron expression (5-field Unix: '0 8 * * *' for 8am daily)")
                        .required(false)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("triggerTime")
                        .type("Long")
                        .description("Unix timestamp in milliseconds for one-time trigger")
                        .required(false)
                        .build()
        );

        return metadata;
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        try {

            Map<String, Object> params = request.getParameters();

            if (params == null) {
                return ToolResponse.builder()
                        .status("FAILED")
                        .message("Parameters are required")
                        .build();
            }

            String reportName = getString(params, "reportName");

            if (reportName == null || reportName.isBlank()) {
                List<ReportConfig> configs = configService.getAllConfigs();
                if (configs != null && !configs.isEmpty()) {
                    reportName = configs.get(0).getReportName();
                    log.warn("reportName missing in schedule_report. Using fallback: {}", reportName);
                } else {
                    return ToolResponse.builder()
                            .status("FAILED")
                            .message("reportName is required and no configurations exist")
                            .build();
                }
            }

            /*
             * Load the config.
             */
            ReportConfig config = configService.getConfigByName(reportName);

            if (config == null) {
                return ToolResponse.builder()
                        .status("FAILED")
                        .message("Report config not found: " + reportName)
                        .build();
            }

            String frequency = getString(params, "frequency");
            String cron      = getString(params, "cron");
            String triggerTimeStr = getString(params, "triggerTime");

            boolean hasSchedule = false;

            /*
             * Apply frequency schedule.
             */
            if (frequency != null && !frequency.isBlank()) {
                config.setFrequency(frequency.toLowerCase().trim());
                config.setCron(null); // Clear any old cron so frequency takes precedence
                config.setSchedulerType("CRON");
                hasSchedule = true;

                log.info(
                        "Scheduling '{}' with frequency: {}",
                        reportName, frequency
                );
            }

            /*
             * Apply explicit cron expression.
             */
            if (cron != null && !cron.isBlank()) {
                config.setCron(normalizeCron(cron.trim()));
                config.setSchedulerType("CRON");
                hasSchedule = true;

                log.info(
                        "Scheduling '{}' with cron: {}",
                        reportName, config.getCron()
                );
            }

            /*
             * Apply trigger-time (legacy one-shot mode).
             */
            if (triggerTimeStr != null && !triggerTimeStr.isBlank()) {
                long triggerTime = Long.parseLong(triggerTimeStr);
                config.setTriggerTime(triggerTime);
                config.setSchedulerType("TRIGGER");
                hasSchedule = true;

                log.info(
                        "Scheduling '{}' with trigger time: {}",
                        reportName, triggerTime
                );
            }

            /*
             * Default to daily if nothing specified.
             */
            if (!hasSchedule) {
                config.setFrequency("daily");
                config.setSchedulerType("CRON");
                log.info(
                        "No schedule specified for '{}'. Defaulting to daily.",
                        reportName
                );
            }

            /*
             * Persist the updated config.
             */
            configService.saveConfig(config);

            /*
             * Immediately register the schedule with the scheduler
             * so it starts running without restart.
             */
            if ("CRON".equalsIgnoreCase(config.getSchedulerType())) {
                schedulerService.registerCronSchedule(config);
            }

            /*
             * Build response message.
             */
            String scheduleDescription = buildScheduleDescription(config);

            Map<String, Object> data = new HashMap<>();
            data.put("reportName", reportName);
            data.put("scheduleType", config.getSchedulerType());
            data.put("frequency", config.getFrequency());
            data.put("cron", config.getCron());
            data.put("description", scheduleDescription);

            return ToolResponse.builder()
                    .status("SUCCESS")
                    .message("Report '" + reportName + "' scheduled successfully. " + scheduleDescription)
                    .data(data)
                    .build();

        } catch (Exception ex) {

            log.error("Failed to schedule report", ex);

            return ToolResponse.builder()
                    .status("FAILED")
                    .message("Failed to schedule report: " + ex.getMessage())
                    .build();
        }
    }

    private String getString(Map<String, Object> params, String key) {
        Object val = params.get(key);
        return val != null ? val.toString() : null;
    }

    private String buildScheduleDescription(ReportConfig config) {
        if ("CRON".equalsIgnoreCase(config.getSchedulerType())) {
            return "Scheduled with cron: " + config.getCron();
        } else if ("TRIGGER".equalsIgnoreCase(config.getSchedulerType())) {
            return "Scheduled for single trigger at timestamp: " + config.getTriggerTime();
        }
        return "No schedule defined";
    }

    /**
     * Normalizes a cron expression to ensure it has 6 fields for Spring CronTrigger.
     * If the LLM generates a 5-field Unix cron (e.g. '* * * * *'), prepends '0 '.
     */
    private String normalizeCron(String cron) {
        if (cron == null || cron.isBlank()) return cron;
        String[] parts = cron.trim().split("\\s+");
        if (parts.length == 5) {
            return "0 " + cron.trim();
        }
        return cron.trim();
    }
}
