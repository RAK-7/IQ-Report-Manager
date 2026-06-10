package IQ_Report_Manager.ai.metrics;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Stores runtime agent metrics.
 *
 * Later this can be replaced by:
 * - Micrometer
 * - Prometheus
 * - Grafana
 */
@Service
public class AgentMetricsService {

    /**
     * Total tool executions.
     */
    private final AtomicLong totalExecutions =
            new AtomicLong();

    /**
     * Successful executions.
     */
    private final AtomicLong successfulExecutions =
            new AtomicLong();

    /**
     * Failed executions.
     */
    private final AtomicLong failedExecutions =
            new AtomicLong();

    public void incrementSuccess() {

        totalExecutions.incrementAndGet();

        successfulExecutions.incrementAndGet();
    }

    public void incrementFailure() {

        totalExecutions.incrementAndGet();

        failedExecutions.incrementAndGet();
    }

    public long getTotalExecutions() {

        return totalExecutions.get();
    }

    public long getSuccessfulExecutions() {

        return successfulExecutions.get();
    }

    public long getFailedExecutions() {

        return failedExecutions.get();
    }

    public double getSuccessRate() {

        long total =
                totalExecutions.get();

        if (total == 0) {
            return 0;
        }

        return (successfulExecutions.get() * 100.0)
                / total;
    }
}