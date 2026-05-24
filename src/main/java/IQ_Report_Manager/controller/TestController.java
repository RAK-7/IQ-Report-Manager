package IQ_Report_Manager.controller;

import IQ_Report_Manager.ai.agent.ReportAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final ReportAgentService reportAgentService;

    @GetMapping("/test-ai")
    public String test() {

        return reportAgentService.process(
                "Generate monthly sales report in XLSX and email it"
        );
    }
}