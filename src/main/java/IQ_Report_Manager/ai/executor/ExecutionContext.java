package IQ_Report_Manager.ai.executor;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ExecutionContext {

    private String conversationId;

    private final Map<String,Object> outputs =
            new HashMap<>();

}