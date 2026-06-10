package IQ_Report_Manager.ai.executor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutionResult {

    private boolean success;

    private String failedTool;

    private String errorMessage;

    private ExecutionContext context;

    private int retryCount;
}