package IQ_Report_Manager.ai.prompt;

import IQ_Report_Manager.ai.config.ReportConfigProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds dynamic prompts
 * for the execution planner.
 */
@Component
@RequiredArgsConstructor
public class ToolPromptBuilder {

    private final ReportConfigProvider reportConfigProvider;

    /**
     * Creates planner prompt
     * with live ReportConfigs.
     */
    public String buildExecutionPrompt() {

        String configContext =
                reportConfigProvider
                        .buildConfigContext();

        return PromptTemplate.EXECUTION_PLAN_PROMPT
                + "\n\n"
                + configContext;
    }
}