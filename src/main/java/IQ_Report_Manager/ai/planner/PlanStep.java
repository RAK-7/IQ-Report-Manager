package IQ_Report_Manager.ai.planner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a single executable step inside an ExecutionPlan.
 *
 * Example:
 *
 * Step 1:
 * generate_report
 *
 * Step 2:
 * publish_report
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanStep {

    /**
     * Execution order.
     */
    private Integer order;

    /**
     * MCP Tool name.
     *
     * Examples:
     * - generate_report
     * - publish_report
     * - schedule_report
     */
    private String tool;

    /**
     * Parameters required by the tool.
     */
    private Map<String, Object> parameters;
}