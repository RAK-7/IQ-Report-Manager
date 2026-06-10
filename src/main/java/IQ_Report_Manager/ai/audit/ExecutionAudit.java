package IQ_Report_Manager.ai.audit;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Stores every agent execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "execution_audit")
public class ExecutionAudit {

    @Id
    private String id;

    /**
     * Conversation identifier.
     */
    private String conversationId;

    /**
     * Executed tool.
     */
    private String toolName;

    /**
     * Tool input.
     */
    private Map<String,Object> parameters;

    /**
     * SUCCESS / FAILED
     */
    private String status;

    /**
     * Error if any.
     */
    private String errorMessage;

    /**
     * Execution time.
     */
    private Long durationMs;

    /**
     * Timestamp.
     */
    private LocalDateTime executedAt;
}