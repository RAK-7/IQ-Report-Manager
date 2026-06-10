package IQ_Report_Manager.controller;

import IQ_Report_Manager.ai.metrics.AgentMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent runtime metrics.
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final AgentMetricsService metricsService;

    @GetMapping
    public Map<String,Object> metrics() {

        Map<String,Object> response =
                new HashMap<>();

        response.put(
                "totalExecutions",
                metricsService.getTotalExecutions()
        );

        response.put(
                "successfulExecutions",
                metricsService.getSuccessfulExecutions()
        );

        response.put(
                "failedExecutions",
                metricsService.getFailedExecutions()
        );

        response.put(
                "successRate",
                metricsService.getSuccessRate()
        );

        return response;
    }
}