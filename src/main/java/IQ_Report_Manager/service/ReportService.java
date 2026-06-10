package IQ_Report_Manager.service;


import IQ_Report_Manager.ai.executor.ReportExecutionResult;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Service
@RequiredArgsConstructor
public final class ReportService {

    private final DataRepositoryFactory dataRepositoryFactory;
    private final PublisherFactory publisherFactory;
    private final ReportGeneratorFactory reportGeneratorFactory;
    private final FileHandlerFactory fileHandlerFactory;

    public void generateReport(ReportConfig config) {

        generateReportWithResult(config);

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

    public ReportExecutionResult generateReportWithResult(
            ReportConfig config
    ) {

        LocalDateTime startTime =
                LocalDateTime.now();

        AtomicLong recordCount =
                new AtomicLong();

        DataRepository datarepo =
                dataRepositoryFactory.getRepository(
                        config.getDbType()
                );

        ReportGenerator generator =
                reportGeneratorFactory.getGenerator(
                        config.getReportType()
                );

        FileHandler handler =
                fileHandlerFactory.getHandler(
                        config.getFileType()
                );

        String fileName =
                config.getReportName()
                        + "_"
                        + System.currentTimeMillis()
                        + "."
                        + config.getFileType()
                        .toLowerCase();

        try {

            handler.init(fileName);

            /*
             * AGG Report Flow
             */
            if ("AGG".equalsIgnoreCase(
                    config.getReportType()
            )) {

                log.info(
                        "Generating AGG report {}",
                        config.getReportName()
                );

                var aggRows =
                        datarepo.fetchAggData(
                                config
                        );

                for (Map<String, Object> sourceRow : aggRows) {

                    try {

                        Map<String, Object> reportRow =
                                generator.processRow(
                                        sourceRow,
                                        config
                                );

                        handler.writeRow(
                                reportRow
                        );

                        recordCount.incrementAndGet();

                    } catch (Exception ex) {

                        log.error(
                                "Error processing AGG row {}",
                                sourceRow,
                                ex
                        );
                    }
                }

            }
            /*
             * RAW Report Flow
             */
            else {

                log.info(
                        "Generating RAW report {}",
                        config.getReportName()
                );

                datarepo.fetchData(
                        config,
                        sourceRow -> {

                            try {

                                Map<String, Object> reportRow =
                                        generator.processRow(
                                                sourceRow,
                                                config
                                        );

                                handler.writeRow(
                                        reportRow
                                );

                                recordCount.incrementAndGet();

                            } catch (Exception ex) {

                                log.error(
                                        "Error processing RAW row {}",
                                        sourceRow,
                                        ex
                                );
                            }
                        }
                );
            }

        } catch (Exception ex) {

            log.error(
                    "Error generating report {}",
                    config.getReportName(),
                    ex
            );

            throw new RuntimeException(
                    "Failed to generate report",
                    ex
            );

        } finally {

            try {

                handler.close();

            } catch (Exception ex) {

                log.error(
                        "Error closing handler",
                        ex
                );
            }
        }

        /*
         * Publish report
         */
        Publisher publisher =
                publisherFactory.getPublisher(
                        config.getPublisher()
                );

        publisher.publish(
                config,
                fileName
        );

        LocalDateTime endTime =
                LocalDateTime.now();

        return ReportExecutionResult.builder()
                .reportName(
                        config.getReportName()
                )
                .fileName(
                        fileName
                )
                .filePath(
                        handler.getFilePath()
                )
                .recordCount(
                        recordCount.get()
                )
                .reportType(
                        config.getReportType()
                )
                .dbType(
                        config.getDbType()
                )
                .fileType(
                        config.getFileType()
                )
                .reportConfig(
                        config
                )
                .startTime(
                        startTime
                )
                .endTime(
                        endTime
                )
                .status(
                        "SUCCESS"
                )
                .build();
    }
}