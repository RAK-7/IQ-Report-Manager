package IQ_Report_Manager.ai.executor;

import IQ_Report_Manager.ai.planner.ExecutionPlan;
import IQ_Report_Manager.ai.planner.PlanStep;
import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.mcp.registry.ToolRegistry;
import IQ_Report_Manager.mcp.tool.McpTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanExecutor {

    private final ToolRegistry toolRegistry;

    public ExecutionContext execute(
            ExecutionPlan plan,
            String conversationId
    ) {

        ExecutionContext context =
                new ExecutionContext();

        context.setConversationId(
                conversationId
        );

        for (PlanStep step : plan.getSteps()) {

            McpTool tool =
                    toolRegistry.getTool(
                            step.getTool()
                    );

            if (tool == null) {

                throw new RuntimeException(
                        "Tool not found: "
                                + step.getTool()
                );
            }

            ToolRequest request =
                    ToolRequest.builder()
                            .toolName(step.getTool())
                            .parameters(step.getParameters())
                            .build();

            ToolResponse response =
                    tool.execute(request);

            context.getOutputs().put(
                    step.getTool(),
                    response
            );
        }

        return context;
    }
}