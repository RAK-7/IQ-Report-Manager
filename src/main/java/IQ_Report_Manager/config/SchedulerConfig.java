package IQ_Report_Manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Scheduler configuration.
 *
 * Provides:
 * - @EnableScheduling for @Scheduled methods
 * - TaskScheduler bean for dynamic cron registration at runtime
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    /**
     * ThreadPoolTaskScheduler allows dynamic scheduling of tasks at runtime.
     * SchedulerService uses this to register new cron jobs without restart.
     *
     * Pool size 5 allows up to 5 reports to execute concurrently.
     */
    @Bean
    public TaskScheduler taskScheduler() {

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("report-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }
}
