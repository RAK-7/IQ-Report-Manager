package IQ_Report_Manager.controller;

import IQ_Report_Manager.ai.agent.ReportAgentService;
import IQ_Report_Manager.ai.dto.AgentRequest;
import IQ_Report_Manager.ai.dto.AgentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final ReportAgentService reportAgentService;

    @GetMapping("/test-ai")
    public AgentResponse test() {

        AgentRequest request = AgentRequest.builder()
                .message(
                        "Generate monthly sales report in XLSX and email it"
                )
                .conversationId("conv-1001")
                .userId("user-1")
                .build();

        return reportAgentService.process(request);
    }
}