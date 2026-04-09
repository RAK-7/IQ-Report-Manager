//package IQ_Report_Manager.repository.data.impl;
//
//import IQ_Report_Manager.model.config.mongo.ReportConfig;
//import IQ_Report_Manager.repository.data.DataRepository;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Repository;
//
//import java.util.*;
//
//@Slf4j
//@Repository
//public class MysqlRepoImpl implements DataRepository {
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @Override
//    public String getDbType() {
//        return "MYSQL";
//    }
//
//    @Override
//    public List<Map<String, Object>> fetchData(ReportConfig config) {
//
//        String tableName = config.getIndex();
//
//        String query = buildQuery(tableName);
//
//        List<Map<String, Object>> queryResult = executeQuery(query);
//
//        return extractResults(queryResult);
//    }
//
//    // Build Query (same role as ES NativeQuery)
//    private String buildQuery(String tableName) {
//        return "SELECT * FROM " + tableName;
//    }
//
//    // Execute Query
//    private List<Map<String, Object>> executeQuery(String query) {
//        try {
//            return jdbcTemplate.queryForList(query);
//        } catch (Exception e) {
//            log.error("Error executing query: {}", query, e);
//            return new ArrayList<>();
//        }
//    }
//
//    // Extract Results (you can simplify this)
//    private List<Map<String, Object>> extractResults(List<Map<String, Object>> queryResult) {
//        return new ArrayList<>(queryResult);
//    }
//
//    //  Bulk Insert
//    public void insertBulkData(int count, String tableName) {
//
//        String sql = "INSERT INTO " + tableName + " (" +
//                "message_id, customer_id, header_id, message, mobile_num, status, wordcount, multipart_size, timestamp," +
//                "pe_id, template_id," +
//                "bind_id, country_code, start_time, deliver_time, submit_time, country_name," +
//                "operator_model, operator_circle, operator_name," +
//                "sub_user_id, account_type" +
//                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//        List<Object[]> batchArgs = new ArrayList<>();
//
//        for (int i = 0; i < count; i++) {
//
//            String messageId = UUID.randomUUID().toString();
//
//            batchArgs.add(new Object[]{
//                    messageId,
//                    "rakesh",
//                    "AJIOLX",
//                    "encoded-message",
//                    "9012345678",
//                    (i % 2 == 0 ? "DELIVERED" : "FAILED"),
//                    10,
//                    1,
//                    new Date(),
//
//                    "1234567890",
//                    "1234567890",
//
//                    "abc",
//                    "+91",
//                    new Date(),
//                    System.currentTimeMillis(),
//                    System.currentTimeMillis(),
//                    "India",
//                    "GSM",
//                    "DL",
//                    "Vodafone Idea Limited",
//
//                    "123",
//                    "CORPORATE"
//            });
//        }
//
//        try {
//            int[] result = jdbcTemplate.batchUpdate(sql, batchArgs);
//            log.info("Bulk insert completed. Rows inserted: {}", result.length);
//
//        } catch (Exception e) {
//            log.error("Bulk insert failed!", e);
//        }
//    }
////    @PostConstruct
////    public void init() {
////        insertBulkData(20, "reports");
////    }
//}