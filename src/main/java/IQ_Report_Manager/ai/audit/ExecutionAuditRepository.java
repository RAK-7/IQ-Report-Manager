package IQ_Report_Manager.ai.audit;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExecutionAuditRepository extends MongoRepository<ExecutionAudit,String> {
}
