package IQ_Report_Manager.repository.data.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.repository.data.DataRepository;
import IQ_Report_Manager.model.data.elastic.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
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
    public List<Map<String, Object>> fetchData(ReportConfig config) {

        String indexName = config.getIndex();

        // this is for building query, it selects from index (fetch data)
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .build();

        // this is executing query that runs query on index and returns SearchHits (list of results)
        SearchHits<Map> searchHits =
                elasticsearchOperations.search(
                        query,
                        Map.class,
                        IndexCoordinates.of(indexName)
                );

        List<Map<String, Object>> results = new ArrayList<>(); // this converts to usable format

        // this is to extract data and here Each hit = one document, getContent() = actual data
        searchHits.forEach(hit -> {
            Map<String, Object> content = (Map<String, Object>) hit.getContent();
            results.add(content);
        });

        return results;
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