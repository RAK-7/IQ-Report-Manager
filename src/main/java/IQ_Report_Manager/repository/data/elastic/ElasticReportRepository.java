package IQ_Report_Manager.repository.data.elastic;

import IQ_Report_Manager.model.data.elastic.ReportDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticReportRepository extends ElasticsearchRepository<ReportDocument, String> {
}
