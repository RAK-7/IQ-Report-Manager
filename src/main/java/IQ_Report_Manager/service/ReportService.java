package IQ_Report_Manager.service;


import IQ_Report_Manager.factory.data.DataRepositoryFactory;
import IQ_Report_Manager.factory.filehandler.FileHandlerFactory;
import IQ_Report_Manager.factory.publisher.PublisherFactory;
import IQ_Report_Manager.filehandler.FileHandler;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.publisher.Publisher;
import IQ_Report_Manager.repository.data.DataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final DataRepositoryFactory dataRepositoryFactory;
    private final PublisherFactory publisherFactory;
    private final FileHandlerFactory fileHandlerFactory;

    public void generateReport(ReportConfig config) {

        //  Get repository
        DataRepository repo =
                dataRepositoryFactory.getRepository(config.getDbType());

        // Get file handler
        FileHandler handler =
                fileHandlerFactory.getHandler(config.getFileType());

        String fileName = "report." + config.getFileType().toLowerCase();

        try {
            // Initialize file
            handler.init(fileName);

            // Stream data
            repo.fetchData(config, handler);

        } catch (Exception e) {
            log.error("Error generating report for file: {} with config: {}", fileName, config, e);
            throw new RuntimeException("Error generating report" + fileName, e);

        } finally {
            try {
                handler.close();
            } catch (Exception e) {
                log.error("Error while closing file handler for file: {}", fileName, e);

            }
        }

        // Publish
        Publisher publisher =
                publisherFactory.getPublisher(config.getPublisher());

        publisher.publish(config, fileName);
    }
}