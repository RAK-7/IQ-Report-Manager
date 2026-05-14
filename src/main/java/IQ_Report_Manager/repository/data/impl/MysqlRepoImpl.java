package IQ_Report_Manager.repository.data.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.repository.data.DataRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Repository
public class MysqlRepoImpl implements DataRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String getDbType() {
        return "MYSQL";
    }

    @Override
    public void fetchData(ReportConfig config, Consumer<Map<String, Object>> consumer) {

        String tableName = config.getIndex();
        String query = "SELECT * FROM " + tableName;

        log.info("Starting data fetch from MySQL table: {}", tableName);

        try {

            // for performance tuning
            jdbcTemplate.setFetchSize(1000);

            jdbcTemplate.query(query, rs -> {

                Map<String, Object> row = new HashMap<>();

                try {
                    int columnCount = rs.getMetaData().getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rs.getMetaData().getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }

                    // Stream row via consumer
                    consumer.accept(row);

                } catch (Exception e) {
                    log.error("Error processing row from MySQL table: {}", tableName, e);
                    throw new RuntimeException("Error processing row", e);
                }

            });

            log.info("Completed data fetch from MySQL table: {}", tableName);

        } catch (Exception e) {
            log.error("Error streaming data from MySQL table: {}", tableName, e);
            throw new RuntimeException("Failed to fetch data from MySQL", e);
        }
    }

    @Override
    public List<Map<String, Object>> fetchAggData(ReportConfig config) {

        try {
            log.info("Executing MySQL aggregation query...");

            // Determine table name.
            // Use config.getIndex() for consistency with ElasticRepoImpl.
            String tableName = config.getIndex();

            // Determine time field (default: timestamp).
            String timeField = config.getTimeField() != null && !config.getTimeField().isBlank()
                    ? config.getTimeField()
                    : "timestamp";

            // Format to minute-level, matching your Elastic aggregation.
            // Example output: 2026-05-11 17:08:00
            String formattedDateExpr = "DATE_FORMAT(" + timeField + ", '%Y-%m-%d %H:%i:00')";

            // Build SQL.
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ")
                    .append(formattedDateExpr).append(" AS Date, ")
                    .append("operator_name AS Operator, ")
                    .append("SUM(CASE WHEN status = 'DELIVERED' THEN 1 ELSE 0 END) AS `Total Delivered`, ")
                    .append("SUM(CASE WHEN status IN ('FAILED', 'DELIVERY_ERROR') THEN 1 ELSE 0 END) AS `Total Failed`, ")
                    .append("SUM(CASE WHEN status = 'SUBMITTED' THEN 1 ELSE 0 END) AS `Total Submitted` ")
                    .append("FROM ").append(tableName);

            // Optional filters.
            List<Object> params = new ArrayList<>();

            if (config.getFilters() != null && !config.getFilters().isEmpty()) {
                sql.append(" WHERE ");

                boolean first = true;
                for (Map.Entry<String, Object> entry : config.getFilters().entrySet()) {
                    if (!first) {
                        sql.append(" AND ");
                    }

                    sql.append(entry.getKey()).append(" = ?");
                    params.add(entry.getValue());

                    first = false;
                }
            }

            // Grouping and ordering.
            sql.append(" GROUP BY ")
                    .append(formattedDateExpr)
                    .append(", operator_name ")
                    .append(" ORDER BY Date, Operator");

            log.info("MySQL Aggregation SQL: {}", sql);
            log.info("MySQL Aggregation Params: {}", params);

            List<Map<String, Object>> rows = jdbcTemplate.query(
                    sql.toString(),
                    params.toArray(),
                    (rs, rowNum) -> {
                        Map<String, Object> row = new LinkedHashMap<>();

                        // Date is already formatted by SQL.
                        row.put("Date", rs.getString("Date"));
                        row.put("Operator", rs.getString("Operator"));
                        row.put("Total Delivered", rs.getLong("Total Delivered"));
                        row.put("Total Failed", rs.getLong("Total Failed"));
                        row.put("Total Submitted", rs.getLong("Total Submitted"));

                        log.info("Generated MySQL Row: {}", row);

                        return row;
                    });

            log.info("MySQL aggregation query executed successfully. Rows: {}", rows.size());

            return rows;

        } catch (Exception e) {
            log.error("FULL MYSQL AGG ERROR", e);
            throw new RuntimeException("Error executing MySQL aggregation query", e);
        }
    }

    // Bulk Insert
    public void insertBulkData(int count, String tableName) {

        String sql = "INSERT INTO " + tableName + " (" +
                "message_id, customer_id, header_id, message, mobile_num, status, wordcount, multipart_size, " +
                "timestamp," + "pe_id, template_id," +
                "bind_id, country_code, start_time, deliver_time, submit_time, country_name," +
                "operator_model, operator_circle, operator_name," +
                "sub_user_id, account_type" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();
        String[] statuses = { "DELIVERED", "FAILED", "SUBMITTED" };

        for (int i = 0; i < count; i++) {

            String messageId = UUID.randomUUID().toString();

            batchArgs.add(new Object[] {
                    messageId,
                    "rakesh",
                    "AJIOLX",
                    "encoded-message",
                    "9012345678",
                    statuses[i % statuses.length],
                    10,
                    1,
                    new Date(),

                    "1234567890",
                    "1234567890",

                    "abc",
                    "+91",
                    new Date(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    "India",
                    "GSM",
                    "DL",
                    "Vodafone Idea Limited",

                    "123",
                    "CORPORATE"
            });
        }

        try {
            int[] result = jdbcTemplate.batchUpdate(sql, batchArgs);
            log.info("Bulk insert completed. Rows inserted: {}", result.length);

        } catch (Exception e) {
            log.error("Bulk insert failed!", e);
        }
    }
    // @PostConstruct
    // public void init() {
    // insertBulkData(2000, "reports");
    // }
}