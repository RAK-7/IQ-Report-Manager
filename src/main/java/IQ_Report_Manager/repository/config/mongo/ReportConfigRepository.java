package IQ_Report_Manager.repository.config.mongo;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportConfigRepository extends MongoRepository<ReportConfig, String> {
}
