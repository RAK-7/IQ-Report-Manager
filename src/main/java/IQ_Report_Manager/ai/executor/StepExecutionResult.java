package IQ_Report_Manager.ai.executor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StepExecutionResult {

    private boolean success;

    private String tool;

    private String message;
}