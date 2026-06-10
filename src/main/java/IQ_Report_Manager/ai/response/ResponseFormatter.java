package IQ_Report_Manager.ai.response;
//Converts structured results into plain language.

import IQ_Report_Manager.ai.dto.AgentResponse;
import IQ_Report_Manager.ai.planner.ExecutionPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Converts technical execution results
 * into human-readable responses.
 */
@Slf4j
@Component
public class ResponseFormatter {

    /**
     * Success response.
     */
    public AgentResponse success(
            String message,
            ExecutionPlan executionPlan
    ) {

        return AgentResponse.builder()
                .status("SUCCESS")
                .message(message)
                .executionPlan(executionPlan)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Failure response.
     */
    public AgentResponse failure(
            String message
    ) {

        return AgentResponse.builder()
                .status("FAILED")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Clarification response.
     */
    public AgentResponse clarification(
            String message
    ) {

        return AgentResponse.builder()
                .status("NEEDS_CLARIFICATION")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
