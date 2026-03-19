package IQ_Report_Manager.service;


import IQ_Report_Manager.factory.data.DataRepositoryFactory;
import IQ_Report_Manager.factory.publisher.PublisherFactory;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.publisher.Publisher;
import IQ_Report_Manager.repository.data.DataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final DataRepositoryFactory dataRepositoryFactory;
    private final PublisherFactory publisherFactory;

    public List<Map<String, Object>> generateReport(ReportConfig config) {

        // Step 1: Fetch data from correct DB
        DataRepository repo =
                dataRepositoryFactory.getRepository(config.getDbType());

        List<Map<String, Object>> data = repo.fetchData(config);

        // Step 2: Get publisher type
        Publisher publisher =
                publisherFactory.getPublisher(config.getPublisher());

        // Step 3: Publish the report (Email etc.)
        publisher.publish(config, data);

        return data;
    }
}
