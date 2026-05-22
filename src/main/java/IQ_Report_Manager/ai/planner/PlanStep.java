package IQ_Report_Manager.ai.planner;
//Represents a single action such as generate_report.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Single execution step.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanStep {

    /**
     * Step order.
     */
    private Integer order;

    /**
     * Action/tool name.
     */
    private String action;

    /**
     * Step description.
     */
    private String description;

    /**
     * Step parameters.
     */
    private Map<String, Object> parameters;

    /**
     * Whether execution is mandatory.
     */
    private boolean required;
}