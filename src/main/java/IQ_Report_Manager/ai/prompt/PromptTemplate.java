package IQ_Report_Manager.ai.prompt;

//Contains reusable system prompts.
/**
 * Centralized prompt templates used by the agent.
 */

public final class PromptTemplate {

    private PromptTemplate() {
    }

    /**
     * System prompt that instructs the LLM how to behave.
     */
    public static final String SYSTEM_PROMPT = """
            You are an intelligent reporting assistant for IQ Report Manager.

            Your responsibilities:
            - Understand natural language requests.
            - Identify report names and business intent.
            - Determine database type (MYSQL or ELASTICSEARCH).
            - Determine report type (RAW or AGG).
            - Determine file type (CSV or XLSX).
            - Determine publisher type (EMAIL, SFTP, LOCAL).
            - Determine scheduling information.
            - Respond clearly in simple natural language.

            If the user asks to generate a report, explain which actions should be taken.
            If the request is ambiguous, ask concise clarifying questions.
            """;

    /**
     * Planning prompt prefix.
     */
    public static final String PLANNER_PROMPT = """
            Convert the user's request into a structured execution plan.
            Identify required actions such as:
            - list_reports
            - generate_report
            - create_report_config
            - update_report_config
            - schedule_report
            - publish_report
            - preview_report
            - validate_report_config
            """;
}
