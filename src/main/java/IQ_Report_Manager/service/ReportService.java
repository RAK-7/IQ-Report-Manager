package IQ_Report_Manager.service;


import IQ_Report_Manager.factory.data.DataRepositoryFactory;
import IQ_Report_Manager.factory.filehandler.FileHandlerFactory;
import IQ_Report_Manager.factory.publisher.PublisherFactory;
import IQ_Report_Manager.filehandler.FileHandler;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.publisher.Publisher;
import IQ_Report_Manager.reportgenerator.ReportGenerator;
import IQ_Report_Manager.factory.reportgenerator.ReportGeneratorFactory;
import IQ_Report_Manager.repository.data.DataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public final class ReportService {

    private final DataRepositoryFactory dataRepositoryFactory;
    private final PublisherFactory publisherFactory;
    private final ReportGeneratorFactory reportGeneratorFactory;
    private final FileHandlerFactory fileHandlerFactory;

    public void generateReport(ReportConfig config) {

        //  Get repository
        DataRepository datarepo = dataRepositoryFactory.getRepository(config.getDbType());

        // Get generator (RAW / AGG)
        ReportGenerator generator = reportGeneratorFactory.getGenerator(config.getReportType());

        // Get file handler
        FileHandler handler = fileHandlerFactory.getHandler(config.getFileType());

        String fileName = "report." + config.getFileType().toLowerCase();

        try {
            // 4. Initialize file
            handler.init(fileName);

            // 5. Generate report based on type

            if (config.getReportType().equalsIgnoreCase("AGG")) {

                log.info("INSIDE AGG FLOW");

                var aggRows = datarepo.fetchAggData(config);

                for (Map<String, Object> sourceRow : aggRows) {

                    try {

                        Map<String, Object> reportRow =
                                generator.processRow(sourceRow, config);

                        handler.writeRow(reportRow);

                    } catch (Exception e) {

                        log.error("Error processing AGG row: {}", sourceRow, e);
                    }
                }

            } else {

                log.info("INSIDE RAW FLOW");

                datarepo.fetchData(config, sourceRow -> {

                    try {

                        Map<String, Object> reportRow = generator.processRow(sourceRow, config);

                        handler.writeRow(reportRow);

                    } catch (Exception e) {

                        log.error("Error processing RAW row: {}", sourceRow, e);
                    }
                });
            }
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