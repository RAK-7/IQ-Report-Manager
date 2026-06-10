package IQ_Report_Manager.ai.memory.model;

import IQ_Report_Manager.ai.executor.ReportExecutionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistent conversation memory.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversation_memory")
public class ConversationMemory {

    @Id
    private String id;

    /**
     * Conversation ID.
     */
    private String conversationId;

    /**
     * User ID.
     */
    private String userId;

    /**
     * Chat history.
     */
    @Builder.Default
    private List<String> messages =
            new ArrayList<>();

    /**
     * Last report used.
     */
    private String lastReportName;

    /**
     * Last config selected.
     */
    private String lastConfigName;

    /**
     * Last intent.
     */
    private String lastIntent;

    /**
     * Last execution result.
     */
    private ReportExecutionResult lastExecutionResult;

    /**
     * Last interaction.
     */
    private LocalDateTime lastInteraction;
}