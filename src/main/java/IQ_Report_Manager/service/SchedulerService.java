package IQ_Report_Manager.service;


import IQ_Report_Manager.model.config.mongo.ReportConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final ConfigService configService;
    private final ReportService reportService;

    /**
     * Calculates next execution time
     * based on frequency.
     */
    private long calculateNextTrigger(
            ReportConfig config
    ) {

        long current =
                config.getTriggerTime();

        String frequency =
                config.getFrequency();

        if (frequency == null) {

            return current +
                    (60 * 60 * 1000L);
        }

        return switch (
                frequency.toLowerCase()
                ) {

            case "daily" ->
                    current +
                            (24 * 60 * 60 * 1000L);

            case "weekly" ->
                    current +
                            (7 * 24 * 60 * 60 * 1000L);

            case "monthly" ->
                    current +
                            (30L * 24 * 60 * 60 * 1000);

            default ->
                    current +
                            (60 * 60 * 1000L);
        };
    }

    @Scheduled(fixedRate = 60000) // check every minute
    public void runReports() {

        List<ReportConfig> configs = configService.getAllConfigs();

        long now = System.currentTimeMillis();

        for (ReportConfig config : configs) {

            if ("TRIGGER".equalsIgnoreCase(config.getSchedulerType())) {

                if (now >= config.getTriggerTime()) {

                    // Run report
                    reportService.generateReport(config);

                    // next run
                    long nextTrigger = calculateNextTrigger(config);

                    config.setTriggerTime(nextTrigger);

                    configService.saveConfig(config);
                }
            }
        }
    }
}
