package IQ_Report_Manager.ai.memory;

//Structured memory object.

import IQ_Report_Manager.ai.executor.ReportExecutionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents conversation memory.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryContext {

    /**
     * Conversation identifier.
     */
    private String conversationId;

    /**
     * User identifier.
     */
    private String userId;

    /**
     * Conversation history.
     */
    @Builder.Default
    private List<String> messages = new ArrayList<>();

    /**
     * Last interaction timestamp.
     */
    private LocalDateTime lastInteraction;

    /**
     * Stores current execution state.
     */
    private String currentState;

    /**
     * Adds message to memory.
     */
    public void addMessage(String message) {

        if (messages == null) {
            messages = new ArrayList<>();
        }

        messages.add(message);
    }

    /**
     * Last generated report.
     */
    private String lastReportName;

    /**
     * Last report execution result.
     */
    private ReportExecutionResult lastExecutionResult;

    /**
     * Last selected configuration.
     */
    private String lastConfigName;

    /**
     * Last user intent.
     */
    private String lastIntent;

    private String lastPublisher;

    private String lastFileType;

    private String lastScheduleFrequency;
}
