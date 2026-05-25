package IQ_Report_Manager.ai.agent;
//Main service that accepts natural language input and returns a natural language response.


import IQ_Report_Manager.ai.dto.AgentRequest;
import IQ_Report_Manager.ai.dto.AgentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Public entry point for AI requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAgentService {

    private final AgentOrchestrator orchestrator;

    /**
     * Processes AI request.
     */
    public AgentResponse process(
            AgentRequest request
    ) {

        log.info(
                "Received AI request: {}",
                request.getMessage()
        );

        return orchestrator.process(request);
    }
}
