package IQ_Report_Manager.ai.planner;

import IQ_Report_Manager.ai.executor.FailureAnalysis;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Generates recovery plans when execution fails.
 */
@Builder
@Service
public class RecoveryPlanner {

    public ExecutionPlan buildRecoveryPlan(
            FailureAnalysis failure
    ) {

        ExecutionPlan plan =
                new ExecutionPlan();

        plan.setPlanId(
                UUID.randomUUID().toString()
        );

        plan.setCreatedAt(
                LocalDateTime.now()
        );

        plan.setStatus(
                "RECOVERY"
        );

        /*
         * generate_report failed
         */
        if ("generate_report".equalsIgnoreCase(
                failure.getFailedTool()
        )) {

            plan.addStep(
                    PlanStep.builder()
                            .order(1)
                            .tool(
                                    "find_report_config"
                            )
                            .required(true)
                            .build()
            );

            plan.addStep(
                    PlanStep.builder()
                            .order(2)
                            .tool(
                                    "generate_report"
                            )
                            .required(true)
                            .build()
            );
        }

        /*
         * publish_report failed
         */
        if ("publish_report".equalsIgnoreCase(
                failure.getFailedTool()
        )) {

            plan.addStep(
                    PlanStep.builder()
                            .order(1)
                            .tool(
                                    "generate_report"
                            )
                            .required(true)
                            .build()
            );

            plan.addStep(
                    PlanStep.builder()
                            .order(2)
                            .tool(
                                    "publish_report"
                            )
                            .required(true)
                            .build()
            );
        }

        return plan;
    }
}