package IQ_Report_Manager.repository.data.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.repository.data.DataRepository;
import IQ_Report_Manager.model.data.elastic.*;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Repository
public class ElasticRepoImpl implements DataRepository {

    @Autowired
    private org.springframework.data.elasticsearch.core.ElasticsearchOperations elasticsearchOperations;


    @Override
    public String getDbType() {
        // Returns "ELASTICSEARCH" so DataRepositoryFactory lookup succeeds
        // when config.dbType = "ELASTICSEARCH"
        return "ELASTICSEARCH";
    }

    @Override
    public void fetchData(ReportConfig config, Consumer<Map<String, Object>> consumer) {

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
                    consumer.accept(hit.getContent());
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

    @Override
    public List<Map<String, Object>> fetchAggData(ReportConfig config) {

        String indexName = config.getIndex();

        // 1. Build BOOL query (optional filters)
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // Example: dynamic filters (map from config)
        if (config.getFilters() != null && !config.getFilters().isEmpty()) {
            config.getFilters().forEach((key, value) -> {
                boolQuery.must(m -> m.term(t -> t
                        .field(key)
                        .value(value.toString())
                ));
            });
        }

        // 2. Build Aggregation

        // Date aggregation
        String timeField =
                config.getTimeField() != null && !config.getTimeField().isBlank()
                        ? config.getTimeField()
                        : "timestamp";

        DateHistogramAggregation dateAgg =
                DateHistogramAggregation.of(d -> d
                        .field(timeField)
                        .calendarInterval(CalendarInterval.Minute) // or Second
                        .format("yyyy-MM-dd HH:mm:ss")
                );

        // Operator aggregation
        TermsAggregation operatorAgg = TermsAggregation.of(t -> t
                .field("systemMetaData.operatorDetails.operator.keyword")
        );

        // Metrics (filters)
        Aggregation deliveredAgg = Aggregation.of(a -> a
                .filter(f -> f
                        .term(t -> t.field("status.keyword").value("DELIVERED"))
                )
        );

        Aggregation failedAgg = Aggregation.of(a -> a
                .filter(f -> f
                        .terms(t -> t
                                .field("status.keyword")
                                .terms(v -> v.value(
                                        List.of(
                                                FieldValue.of("FAILED"),
                                                FieldValue.of("DELIVERY_ERROR")
                                        )
                                ))
                        )
                )
        );

        Aggregation submitAgg = Aggregation.of(a -> a
                .filter(f -> f
                        .term(t -> t.field("status.keyword").value("SUBMITTED"))
                )
        );

        // Nest aggregations
        Map<String, Aggregation> operatorSubAggs = new HashMap<>();
        operatorSubAggs.put("deliv", deliveredAgg);
        operatorSubAggs.put("failure", failedAgg);
        operatorSubAggs.put("submit", submitAgg);

        Aggregation operatorAggregation = Aggregation.of(a -> a
                .terms(operatorAgg)
                .aggregations(operatorSubAggs)
        );

        Map<String, Aggregation> dateSubAggs = new HashMap<>();
        dateSubAggs.put("by_operator", operatorAggregation);

        Aggregation dateAggregation = Aggregation.of(a -> a
                .dateHistogram(dateAgg)
                .aggregations(dateSubAggs)
        );

        // 3. Build Query
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQuery.build()))
                .withAggregation("by_date", dateAggregation)
                .build();

        try {

            log.info("Executing aggregation query...");

            SearchResponse<Void> response =
                    elasticsearchClient.search(
                            s -> s
                                    .index(indexName)
                                    .query(query.getQuery())
                                    .aggregations(query.getAggregations()),
                            Void.class
                    );

            log.info("Aggregation query executed successfully");

            return parseAggregation(response);

        } catch (Exception e) {

            log.error("FULL AGG ERROR", e);

            throw new RuntimeException(e);
        }
    }

    private List<Map<String, Object>> parseAggregation(SearchResponse<Void> response) {
        log.info("INSIDE parseAggregation()");
        List<Map<String, Object>> rows = new ArrayList<>();

        List<DateHistogramBucket> dateBuckets =
                response.aggregations()
                        .get("by_date")
                        .dateHistogram()
                        .buckets()
                        .array();

        for (DateHistogramBucket dateBucket : dateBuckets) {

            long timestamp = dateBucket.key();
            Instant instant = Instant.ofEpochMilli(timestamp);
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
            String date = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            List<StringTermsBucket> operatorBuckets =
                    dateBucket.aggregations()
                            .get("by_operator")
                            .sterms()
                            .buckets()
                            .array();

            for (StringTermsBucket operatorBucket : operatorBuckets) {

                String operator =
                        operatorBucket.key().stringValue();

                Aggregate deliveredAgg =
                        operatorBucket.aggregations().get("deliv");

                Aggregate failedAgg =
                        operatorBucket.aggregations().get("failure");

                Aggregate submitAgg =
                        operatorBucket.aggregations().get("submit");

                long delivered =
                        deliveredAgg != null
                                ? deliveredAgg.filter().docCount()
                                : 0;

                long failed =
                        failedAgg != null
                                ? failedAgg.filter().docCount()
                                : 0;

                long submitted =
                        submitAgg != null
                                ? submitAgg.filter().docCount()
                                : 0;

                Map<String, Object> row = new LinkedHashMap<>();

                row.put("Date", date);
                row.put("Operator", operator);
                row.put("Total Delivered", delivered);
                row.put("Total Failed", failed);
                row.put("Total Submitted", submitted);

                log.info("Generated Row: {}", row);
                rows.add(row);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("rows", rows);

        return rows;
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
            String[] statuses = {"DELIVERED", "FAILED", "SUBMITTED"};
            report.setStatus(statuses[i % statuses.length]);
            report.setWordcount(10);
            report.setMultipartSize(1);
            report.setTimestamp(new Date());

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
            sys.setCountryName("India");

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
//        insertBulkData(2000, "reports");
//    }
}