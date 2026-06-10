package IQ_Report_Manager.controller;

import IQ_Report_Manager.ai.agent.ReportAgentService;
import IQ_Report_Manager.ai.memory.MemoryContext;
import IQ_Report_Manager.ai.planner.AgentPlanner;
import IQ_Report_Manager.ai.planner.ExecutionPlan;
import IQ_Report_Manager.ai.planner.ExecutionPlanner;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final ReportAgentService reportAgentService;

//    @GetMapping("/test-ai")
//    public AgentResponse test() {
//
//        AgentRequest request = AgentRequest.builder()
//                .message(
//                        "Generate monthly agg report from ES in XLSX and email it to rahulkarn98420@gmail.com"
//                )
//                .conversationId("conv-1001")
//                .userId("user-1")
//                .build();
//
//        return reportAgentService.process(request);
//    }
private final ExecutionPlanner executionPlanner;

    @GetMapping("/test-llm")
    public ExecutionPlan test() {

        return executionPlanner.createPlan(
                "Generate agg report and email it every Monday to rahulkarn98420@gmail.com"
        );
    }
}