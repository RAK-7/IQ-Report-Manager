package IQ_Report_Manager.ai.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import IQ_Report_Manager.ai.llm.LlmService;
import IQ_Report_Manager.ai.prompt.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Converts natural language requests into executable plans.
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionPlanner {

    private final LlmService llmService;

    private final ObjectMapper objectMapper;

    /**
     * Creates an executable plan from natural language.
     */
    public ExecutionPlan createPlan(String userRequest) {

        try {

            String response =
                    llmService.generate(
                            PromptTemplate.EXECUTION_PLAN_PROMPT,
                            userRequest
                    );

            // Qwen often wraps JSON in markdown
            response = response
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            log.info("Execution Plan JSON: {}", response);

            ExecutionPlan plan =
                    objectMapper.readValue(
                            response,
                            ExecutionPlan.class
                    );

            // Populate metadata
            plan.setPlanId(
                    UUID.randomUUID().toString()
            );

            plan.setUserRequest(userRequest);

            plan.setCreatedAt(
                    LocalDateTime.now()
            );

            plan.setStatus("CREATED");

            // Sort execution steps by order
            plan.getSteps()
                    .sort(
                            java.util.Comparator.comparing(
                                    PlanStep::getOrder
                            )
                    );

            /**
             * Basic validation.
             */
            if (plan.getSteps() == null
                    || plan.getSteps().isEmpty()) {

                throw new RuntimeException(
                        "Execution plan contains no steps"
                );
            }

            return plan;

        } catch (Exception ex) {

            log.error(
                    "Failed to create execution plan",
                    ex
            );

            throw new RuntimeException(
                    "Failed to create execution plan",
                    ex
            );
        }
    }
}