package IQ_Report_Manager.ai.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExecutionAuditService {

    private final ExecutionAuditRepository repository;

    public void saveAudit(
            String conversationId,
            String toolName,
            Map<String,Object> parameters,
            String status,
            String error,
            long duration
    ) {

        repository.save(
                ExecutionAudit.builder()
                        .conversationId(conversationId)
                        .toolName(toolName)
                        .parameters(parameters)
                        .status(status)
                        .errorMessage(error)
                        .durationMs(duration)
                        .executedAt(LocalDateTime.now())
                        .build()
        );
    }
}