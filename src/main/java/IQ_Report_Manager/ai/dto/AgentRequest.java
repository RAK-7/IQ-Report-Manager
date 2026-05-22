package IQ_Report_Manager.ai.dto;

//Incoming message payload.
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Incoming request from frontend/API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequest {

    /**
     * Natural language request.
     */
    @NotBlank(message = "message cannot be empty")
    private String message;

    /**
     * Optional conversation tracking ID.
     */
    private String conversationId;

    /**
     * Optional user ID.
     */
    private String userId;
}
