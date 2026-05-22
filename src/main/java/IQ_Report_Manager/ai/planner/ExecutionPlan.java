package IQ_Report_Manager.ai.planner;
//Represents a sequence of tool calls.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Full execution workflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionPlan {

    /**
     * Plan ID.
     */
    private String planId;

    /**
     * Original user request.
     */
    private String userRequest;

    /**
     * Execution steps.
     */
    @Builder.Default
    private List<PlanStep> steps = new ArrayList<>();

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Current status.
     */
    private String status;

    /**
     * Adds a step.
     */
    public void addStep(PlanStep step) {

        if (steps == null) {
            steps = new ArrayList<>();
        }

        steps.add(step);
    }
}
