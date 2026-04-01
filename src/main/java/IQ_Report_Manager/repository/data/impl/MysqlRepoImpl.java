package IQ_Report_Manager.repository.data.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.repository.data.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MysqlRepoImpl implements DataRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String getDbType() {
        return "MYSQL";
    }

    @Override
    public List<Map<String, Object>> fetchData(ReportConfig config) {

        String tableName = config.getIndex();

        //  Build query (like ES NativeQuery)
        String query = buildQuery(tableName);

        //  Execute query (like elasticsearchOperations.search)
        List<Map<String, Object>> queryResult = executeQuery(query);

        //  Extract results
        List<Map<String, Object>> results = extractResults(queryResult);

        return results;
    }

    //  Build query (equivalent to NativeQuery.builder())
    private String buildQuery(String tableName) {
        return "SELECT * FROM " + tableName;
    }

    //  Execute query
    private List<Map<String, Object>> executeQuery(String query) {
        try {
            return jdbcTemplate.queryForList(query);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    //  Extract results (same as  SearchHits loop in es)
    private List<Map<String, Object>> extractResults(List<Map<String, Object>> queryResult) {

        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> row : queryResult) {

            Map<String, Object> map = new HashMap<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }

            results.add(map);
        }

        return results;
    }
}