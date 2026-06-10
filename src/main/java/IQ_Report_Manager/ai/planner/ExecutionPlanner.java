package IQ_Report_Manager.ai.planner;

import IQ_Report_Manager.ai.prompt.ToolPromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import IQ_Report_Manager.ai.llm.LlmService;
import IQ_Report_Manager.ai.prompt.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import IQ_Report_Manager.ai.memory.MemoryContext;
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

    private final ToolPromptBuilder toolPromptBuilder;

    public ExecutionPlan createPlan(String userRequest) {

        return createPlan(
                userRequest,
                null
        );
    }

    /**
     * Creates an executable plan from natural language.
     */
    public ExecutionPlan createPlan(String userRequest, MemoryContext memoryContext) {

        try {

            String plannerPrompt = toolPromptBuilder.buildExecutionPrompt();

            /**
             * Build memory context for planner.
             */
            String memoryPrompt = "";

            if (memoryContext != null) {

                if (memoryContext.getLastReportName() != null) {

                    memoryPrompt += """
                
                PREVIOUS REPORT:
                %s
                
                """
                            .formatted(
                                    memoryContext.getLastReportName()
                            );
                }

                if (memoryContext.getLastIntent() != null) {

                    memoryPrompt += """
                
                PREVIOUS INTENT:
                %s
                
                """
                            .formatted(
                                    memoryContext.getLastIntent()
                            );
                }
            }

            String finalRequest =
                    memoryPrompt
                            + "\n"
                            + userRequest;

            String response =
                    llmService.generate(
                            plannerPrompt,
                            finalRequest
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