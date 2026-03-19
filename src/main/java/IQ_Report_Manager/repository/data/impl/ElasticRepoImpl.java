package IQ_Report_Manager.repository.data.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.repository.data.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Repository;

import java.util.*;

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
}