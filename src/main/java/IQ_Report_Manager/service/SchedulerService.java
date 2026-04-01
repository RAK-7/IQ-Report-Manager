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
    private ReportService reportService;

    @Scheduled(fixedRate = 60000) // check every minute
    public void runReports() {

        List<ReportConfig> configs = configService.getAllConfigs();

        long now = System.currentTimeMillis();

        for (ReportConfig config : configs) {

            if ("TRIGGER".equalsIgnoreCase(config.getSchedulerType())) {

                if (now >= config.getTriggerTime()) {

                    // Run report
                    reportService.generateReport(config);

                    // next run after 1 hour
                    long nextTrigger = config.getTriggerTime() + (60 * 60 * 1000);

                    config.setTriggerTime(nextTrigger);

                    configService.saveConfig(config);
                }
            }
        }
    }
}
