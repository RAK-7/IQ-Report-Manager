package IQ_Report_Manager.service;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.publisher.Publisher;
import IQ_Report_Manager.factory.publisher.PublisherFactory;
import IQ_Report_Manager.ai.executor.ReportExecutionResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Dynamic report scheduler service.
 *
 * Responsibilities:
 * - On startup: load all configs with cron/frequency from MongoDB and register them.
 * - At runtime: register new schedules when ScheduleReportTool saves one.
 * - Every minute: also check trigger-time-based schedules (legacy mode).
 *
 * Supports:
 * - Cron-based scheduling (preferred): "0 8 * * *" → daily at 8am
 * - Frequency-based: daily, weekly, monthly → converted to cron
 * - Trigger-time-based: fire at specific Unix timestamp (legacy)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final ConfigService configService;
    private final ReportService reportService;
    private final PublisherFactory publisherFactory;
    private final TaskScheduler taskScheduler;

    /**
     * Tracks active cron jobs so we can cancel/update them.
     * Key: reportName, Value: active ScheduledFuture
     */
    private final Map<String, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();

    /**
     * On startup: register all configs that have a cron/frequency schedule.
     */
    @PostConstruct
    public void initSchedules() {

        log.info("Initializing scheduled reports from database...");

        try {

            List<ReportConfig> configs = configService.getAllConfigs();

            if (configs == null || configs.isEmpty()) {
                log.info("No scheduled report configurations found at startup.");
                return;
            }

            int registered = 0;

            for (ReportConfig config : configs) {

                if (hasSchedule(config)) {
                    registerCronSchedule(config);
                    registered++;
                }
            }

            log.info("Registered {} scheduled report(s) at startup.", registered);

        } catch (Exception ex) {
            log.warn("Failed to initialize schedules at startup (DB may not be ready yet): {}", ex.getMessage());
        }
    }

    /**
     * Registers or re-registers a report's cron schedule.
     * Called by ScheduleReportTool when user schedules a report.
     *
     * @param config the ReportConfig containing cron or frequency
     */
    public void registerCronSchedule(ReportConfig config) {

        String reportName = config.getReportName();
        String cronExpression = resolveCronExpression(config);

        if (cronExpression == null) {
            log.warn("Cannot schedule '{}' — no cron or frequency defined.", reportName);
            return;
        }

        // Cancel any existing schedule for this report
        cancelSchedule(reportName);

        try {

            CronTrigger cronTrigger = new CronTrigger(
                    cronExpression,
                    TimeZone.getDefault()
            );

            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> executeScheduledReport(config),
                    cronTrigger
            );

            activeTasks.put(reportName, future);

            log.info(
                    "Scheduled report '{}' with cron '{}'",
                    reportName,
                    cronExpression
            );

        } catch (Exception ex) {
            log.error(
                    "Failed to schedule report '{}': {}",
                    reportName,
                    ex.getMessage()
            );
        }
    }

    /**
     * Cancels an existing schedule for a report.
     */
    public void cancelSchedule(String reportName) {

        ScheduledFuture<?> existing = activeTasks.get(reportName);

        if (existing != null && !existing.isCancelled()) {
            existing.cancel(false);
            activeTasks.remove(reportName);
            log.info("Cancelled existing schedule for report '{}'", reportName);
        }
    }

    /**
     * Legacy: check every minute for trigger-time-based schedules.
     * Also reloads cron schedules that may not have been registered.
     */
    @Scheduled(fixedRate = 60000)
    public void runTriggerTimeReports() {

        try {

            List<ReportConfig> configs = configService.getAllConfigs();

            if (configs == null) return;

            long now = System.currentTimeMillis();

            for (ReportConfig config : configs) {

                // Register cron schedule if not already active
                if (hasSchedule(config)
                        && !activeTasks.containsKey(config.getReportName())) {
                    registerCronSchedule(config);
                }

                // Legacy trigger-time mode
                if ("TRIGGER".equalsIgnoreCase(config.getSchedulerType())
                        && config.getTriggerTime() > 0
                        && now >= config.getTriggerTime()) {

                    log.info(
                            "Trigger-time reached for report '{}'. Executing.",
                            config.getReportName()
                    );

                    executeScheduledReport(config);

                    // Advance trigger time to next cycle
                    long nextTrigger = calculateNextTrigger(config);
                    config.setTriggerTime(nextTrigger);
                    configService.saveConfig(config);
                }
            }

        } catch (Exception ex) {
            log.error("Error in trigger-time scheduler check: {}", ex.getMessage());
        }
    }

    /**
     * Executes a scheduled report: generate file + publish.
     */
    private void executeScheduledReport(ReportConfig config) {

        log.info(
                "Executing scheduled report: '{}' (db={}, type={}, file={})",
                config.getReportName(),
                config.getDbType(),
                config.getReportType(),
                config.getFileType()
        );

        try {

            // Generate the report file
            ReportExecutionResult result = reportService.generateReportWithResult(config);

            // Publish (email) the report
            Publisher publisher = publisherFactory.getPublisher(config.getPublisher());
            publisher.publish(config, result.getFileName());

            log.info(
                    "Scheduled report '{}' completed successfully. File: {}, Records: {}",
                    config.getReportName(),
                    result.getFileName(),
                    result.getRecordCount()
            );

        } catch (Exception ex) {
            log.error(
                    "Scheduled report '{}' failed: {}",
                    config.getReportName(),
                    ex.getMessage(),
                    ex
            );
        }
    }

    /**
     * Determines if a config has a valid schedule.
     */
    private boolean hasSchedule(ReportConfig config) {

        if (config == null) return false;

        return (config.getCron() != null && !config.getCron().isBlank())
                || (config.getFrequency() != null && !config.getFrequency().isBlank());
    }

    /**
     * Resolves the cron expression from config.
     * Uses explicit cron if set; otherwise derives from frequency.
     */
    private String resolveCronExpression(ReportConfig config) {

        if (config.getCron() != null && !config.getCron().isBlank()) {
            // Spring @Scheduled uses 6-field cron (with seconds). 
            // If user provides 5-field Unix cron, prepend "0 "
            String cron = config.getCron().trim();
            if (cron.split("\\s+").length == 5) {
                cron = "0 " + cron;
            }
            return cron;
        }

        if (config.getFrequency() != null && !config.getFrequency().isBlank()) {
            return frequencyToCron(config.getFrequency());
        }

        return null;
    }

    /**
     * Converts frequency to a 6-field Spring cron expression.
     * daily   → every day at 8am
     * weekly  → every Monday at 8am
     * monthly → 1st of month at 8am
     */
    private String frequencyToCron(String frequency) {
        return switch (frequency.toLowerCase().trim()) {
            case "daily"   -> "0 0 8 * * *";     // every day at 08:00
            case "weekly"  -> "0 0 8 * * MON";   // every Monday at 08:00
            case "monthly" -> "0 0 8 1 * *";      // 1st of month at 08:00
            case "hourly"  -> "0 0 * * * *";      // every hour on the hour
            case "minute"  -> "0 * * * * *";      // every minute
            default        -> "0 0 8 * * *";
        };
    }

    /**
     * Calculates next trigger time for legacy trigger-time mode.
     */
    private long calculateNextTrigger(ReportConfig config) {

        long current = config.getTriggerTime();
        String frequency = config.getFrequency();

        if (frequency == null) {
            return current + (60 * 60 * 1000L);
        }

        return switch (frequency.toLowerCase()) {
            case "daily"   -> current + (24L * 60 * 60 * 1000);
            case "weekly"  -> current + (7L * 24 * 60 * 60 * 1000);
            case "monthly" -> current + (30L * 24 * 60 * 60 * 1000);
            default        -> current + (60 * 60 * 1000L);
        };
    }
}
