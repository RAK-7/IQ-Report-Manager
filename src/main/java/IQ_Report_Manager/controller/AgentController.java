package IQ_Report_Manager.controller;

import IQ_Report_Manager.ai.agent.ReportAgentService;
import IQ_Report_Manager.ai.dto.AgentRequest;
import IQ_Report_Manager.ai.dto.AgentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST entry point for the AI agent.
 *
 * POST /agent/chat  — send a natural language message,
 *                     get a natural language response back.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/agent")
public class AgentController {

    private final ReportAgentService reportAgentService;

    /**
     * Natural Language → Full Pipeline → Natural Language Response.
     *
     * Example request body:
     * {
     *   "message": "Generate the sales report and email it to john@example.com",
     *   "conversationId": "optional-uuid",
     *   "userId": "optional-user-id"
     * }
     */
    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(
            @Valid @RequestBody AgentRequest request
    ) {
        log.info(
                "Received agent chat request: {}",
                request.getMessage()
        );

        AgentResponse response = reportAgentService.process(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("IQ Report Manager Agent is running.");
    }
}
