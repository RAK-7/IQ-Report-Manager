package IQ_Report_Manager.ai.agent;
//Main service that accepts natural language input and returns a natural language response.

import IQ_Report_Manager.ai.planner.AgentPlanner;
import IQ_Report_Manager.ai.prompt.PromptTemplate;
import IQ_Report_Manager.ai.llm.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Main entry point for processing user requests.
 * This service:
 * 1. Creates an execution plan.
 * 2. Generates a natural language response.
 * 3. (In later phases) executes tools.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAgentService {

    private final AgentPlanner agentPlanner;
    private final LlmService llmService;

    /**
     * Processes a natural language request.
     *
     * @param userMessage user input
     * @return natural language response
     */
    public String process(String userMessage) {

        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("User message cannot be empty");
        }

        log.info("Processing agent request: {}", userMessage);

        // Step 1: Build a textual execution plan
        String executionPlan = agentPlanner.createPlan(userMessage);

        // Step 2: Ask LLM to explain the plan in simple natural language
        String finalPrompt = """
                The following execution plan was generated:

                %s

                Explain in simple natural language what will be done.
                If clarification is required, ask the user clearly.
                """.formatted(executionPlan);

        String response = llmService.generate(
                PromptTemplate.SYSTEM_PROMPT,
                finalPrompt
        );

        log.info("Agent response generated successfully");

        return response;
    }
}
