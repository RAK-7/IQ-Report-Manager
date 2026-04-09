package IQ_Report_Manager.repository.data.impl;

import IQ_Report_Manager.filehandler.FileHandler;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.repository.data.DataRepository;
import IQ_Report_Manager.model.data.elastic.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Repository;
import jakarta.annotation.PostConstruct;

import java.util.*;

@Slf4j
@Repository
public class ElasticRepoImpl implements DataRepository {

    @Autowired
    private org.springframework.data.elasticsearch.core.ElasticsearchOperations elasticsearchOperations;


    @Override
    public String getDbType() {
        return "ES";
    }

    @Override
    public void fetchData(ReportConfig config, FileHandler fileHandler) {

        String indexName = config.getIndex();
        int batchSize = 1000;

        SearchHitsIterator<Map> iterator = null;

        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.matchAll(m -> m))
                    .withPageable(org.springframework.data.domain.PageRequest.of(0, batchSize))
                    .build();

            iterator = elasticsearchOperations.searchForStream(
                    query,
                    Map.class,
                    IndexCoordinates.of(indexName)
            );

            while (iterator.hasNext()) {
                var hit = iterator.next();

                if (hit != null && hit.getContent() != null) {
                    fileHandler.writeRow(hit.getContent());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching data", e);

        } finally {
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (Exception ignored) {}
            }
        }
    }

    @Autowired
    private co.elastic.clients.elasticsearch.ElasticsearchClient elasticsearchClient;
    public void insertBulkData(int count, String indexName) {

        List<co.elastic.clients.elasticsearch.core.bulk.BulkOperation> bulkOperations = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            ReportDocument report = new ReportDocument();

            report.setMessageId(UUID.randomUUID().toString());
            report.setCustomerId("rakesh");
            report.setHeaderId("AJIOLX");
            report.setMessage("encoded-message");
            report.setMobileNum("9012345678");
            report.setStatus(i % 2 == 0 ? "DELIVERED" : "FAILED");
            report.setWordcount(10);
            report.setMultipartSize(1);
            report.setTimestamp("2026-03-25 04:47:58");

            TemplateDetails template = new TemplateDetails();
            template.setPeId("1234567890");
            template.setTemplateId("1234567890");
            report.setTemplateDetails(template);

            OperatorDetails operator = new OperatorDetails();
            operator.setModel("GSM");
            operator.setCircle("DL");
            operator.setOperator("Vodafone Idea Limited");

            SystemMetaData sys = new SystemMetaData();
            sys.setBindId("abc");
            sys.setOperatorDetails(operator);
            sys.setCountryCode("+91");
            sys.setStartTime("2026-03-25 04:47:58");
            sys.setDeliverTime(System.currentTimeMillis());
            sys.setSubmitTime(System.currentTimeMillis());
            sys.setCountryName("");

            report.setSystemMetaData(sys);

            Metadata metadata = new Metadata();
            metadata.setSubUserId("123");
            metadata.setAccountType("CORPORATE");
            report.setMetadata(metadata);

            // Add to bulk operation
            bulkOperations.add(
                    co.elastic.clients.elasticsearch.core.bulk.BulkOperation.of(b -> b
                            .index(idx -> idx
                                    .index(indexName)
                                    .id(report.getMessageId())
                                    .document(report)
                            )
                    )
            );
        }

        try {
            co.elastic.clients.elasticsearch.core.BulkResponse response =
                    elasticsearchClient.bulk(b -> b.operations(bulkOperations));

            if (!response.errors()) {
                System.out.println("Bulk insert successful!");
                return;
            }

            System.out.println("Bulk insert had errors!");

            response.items().forEach(item -> {
                if (item.error() != null) {
                    System.out.println(item.error().reason());
                }
            });

        } catch (Exception e) {
            log.error("Bulk insert failed!", e);
        }
    }

//    @PostConstruct
//    public void init() {
//        insertBulkData(20, "reports");
//    }
}