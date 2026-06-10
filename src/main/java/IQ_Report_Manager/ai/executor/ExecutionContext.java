package IQ_Report_Manager.ai.executor;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared execution state between tool executions.
 *
 * Allows one tool output to become
 * another tool input.
 */
@Data
public class ExecutionContext {

    /**
     * Conversation identifier.
     */
    private String conversationId;

    /**
     * Stores outputs from executed tools.
     *
     * Example:
     *
     * find_report_config
     *     -> ReportConfig
     *
     * generate_report
     *     -> fileName
     */
    private final Map<String, Object> outputs =
            new HashMap<>();

    /**
     * Store a value.
     */
    public void put(
            String key,
            Object value
    ) {
        outputs.put(key, value);
    }

    /**
     * Retrieve a value.
     */
    public Object get(
            String key
    ) {
        return outputs.get(key);
    }
}