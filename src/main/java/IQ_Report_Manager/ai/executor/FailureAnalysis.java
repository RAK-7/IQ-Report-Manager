package IQ_Report_Manager.ai.executor;

import lombok.Builder;
import lombok.Data;

/**
 * Captures execution failure details.
 */
@Data
@Builder
public class FailureAnalysis {

    private String failedTool;

    private String errorMessage;

    private String recoveryStrategy;
}