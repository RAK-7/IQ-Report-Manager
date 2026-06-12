package IQ_Report_Manager.mcp.tool;

//Creates a new configuration from natural language extracted parameters.

import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a new ReportConfig from NL-extracted parameters.
 *
 * The LLM extracts all relevant fields from the user's natural language request
 * and this tool persists them as a new ReportConfig in MongoDB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateReportConfigTool implements McpTool {

    private final ConfigService configService;

    @Override
    public String getName() {
        return "create_report_config";
    }

    @Override
    public ToolMetadata getMetadata() {

        ToolMetadata metadata =
                ToolMetadata.builder()
                        .toolName("create_report_config")
                        .description("Create a new report configuration from natural language extracted parameters")
                        .build();

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportName")
                        .type("String")
                        .description("Name of report (e.g. 'sales', 'operator_daily')")
                        .required(true)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("dbType")
                        .type("String")
                        .description("Database type: MYSQL or ELASTICSEARCH")
                        .required(true)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportType")
                        .type("String")
                        .description("Report type: RAW or AGG")
                        .required(true)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("fileType")
                        .type("String")
                        .description("Output file type: CSV or XLSX")
                        .required(false)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("publisher")
                        .type("String")
                        .description("Publisher type: Email, SFTP, or LOCAL")
                        .required(false)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("email")
                        .type("String")
                        .description("Primary recipient email address")
                        .required(false)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("cc")
                        .type("String")
                        .description("Comma-separated CC email addresses")
                        .required(false)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("bcc")
                        .type("String")
                        .description("Comma-separated BCC email addresses")
                        .required(false)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("index")
                        .type("String")
                        .description("Table name (MySQL) or index name (Elasticsearch)")
                        .required(false)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("frequency")
                        .type("String")
                        .description("Schedule frequency: daily, weekly, or monthly")
                        .required(false)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("cron")
                        .type("String")
                        .description("Cron expression (e.g. '0 8 * * *' for 8am daily)")
                        .required(false)
                        .build()
        );

        return metadata;
    }

    @Override
    public ToolResponse execute(
            ToolRequest request
    ) {

        try {

            Map<String, Object> params = request.getParameters();

            if (params == null || params.isEmpty()) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message("Parameters are required to create a report configuration")
                        .build();
            }

            String reportName = getString(params, "reportName");

            /*
             * Auto-derive reportName if the LLM forgot to include it.
             * This is common with small local models (qwen2.5:7b).
             * Pattern: "{dbType}-{reportType}-report"  e.g. "mysql-agg-report"
             */
            if (reportName == null || reportName.isBlank()) {

                String dbType     = getString(params, "dbType");
                String reportType = getString(params, "reportType");
                String index      = getString(params, "index");

                if (dbType != null && reportType != null) {
                    // e.g. "mysql-agg-report" or "es-raw-report"
                    String dbShort = dbType.toLowerCase().contains("elastic") ? "es" : dbType.toLowerCase();
                    reportName = dbShort + "-" + reportType.toLowerCase() + "-report";
                } else if (index != null && !index.isBlank()) {
                    reportName = index.toLowerCase().trim() + "-report";
                } else {
                    reportName = "auto-report-" + System.currentTimeMillis();
                }

                log.info(
                        "reportName not provided by LLM — auto-derived: '{}'",
                        reportName
                );
            }

            /*
             * Check if a config with this name already exists.
             */
            ReportConfig existing = configService.getConfigByName(reportName);

            if (existing != null) {

                log.info(
                        "Config '{}' already exists, returning existing config",
                        reportName
                );

                Map<String, Object> data = new HashMap<>();
                data.put("reportName", existing.getReportName());
                data.put("reportConfig", existing);

                return ToolResponse.builder()
                        .status("SUCCESS")
                        .message("Report configuration already exists: " + reportName)
                        .data(data)
                        .build();
            }

            /*
             * Build the new ReportConfig from extracted parameters.
             */
            ReportConfig config = new ReportConfig();

            config.setReportName(reportName.toLowerCase().trim());

            // Database type — default to ELASTICSEARCH if not specified
            String dbType = getString(params, "dbType");
            if (dbType == null || dbType.isBlank()) {
                config.setDbType("ELASTICSEARCH");
            } else {
                config.setDbType(dbType.toUpperCase().trim());
            }

            // Report type — default to RAW
            String reportType = getString(params, "reportType");
            if (reportType != null && !reportType.isBlank()) {
                String rtUpper = reportType.toUpperCase().trim();
                if (rtUpper.startsWith("AGG") || rtUpper.equals("AGGREGATE")) {
                    config.setReportType("AGG");
                } else if (rtUpper.equals("RAW")) {
                    config.setReportType("RAW");
                } else {
                    config.setReportType(rtUpper);
                }
            } else {
                config.setReportType("RAW");
            }

            // File type — default to XLSX
            String fileType = getString(params, "fileType");
            if (fileType == null || fileType.isBlank()) {
                config.setFileType("XLSX");
            } else {
                config.setFileType(fileType.toUpperCase().trim());
            }

            // Publisher — default to Email
            String publisher = getString(params, "publisher");
            if (publisher == null || publisher.isBlank()) {
                config.setPublisher("Email");
            } else {
                config.setPublisher(publisher.trim());
            }

            // Primary email recipient
            String email = getString(params, "email");
            if (email != null && !email.isBlank()) {
                config.setEmail(email.trim());
            }

            // CC — support comma-separated string or list
            Object ccObj = params.get("cc");
            if (ccObj != null) {
                if (ccObj instanceof List<?> ccList) {
                    config.setCc(ccList.stream()
                            .map(Object::toString)
                            .filter(s -> !s.isBlank())
                            .toList());
                } else {
                    String ccStr = ccObj.toString();
                    if (!ccStr.isBlank()) {
                        config.setCc(Arrays.stream(ccStr.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .toList());
                    }
                }
            }

            // BCC — support comma-separated string or list
            Object bccObj = params.get("bcc");
            if (bccObj != null) {
                if (bccObj instanceof List<?> bccList) {
                    config.setBcc(bccList.stream()
                            .map(Object::toString)
                            .filter(s -> !s.isBlank())
                            .toList());
                } else {
                    String bccStr = bccObj.toString();
                    if (!bccStr.isBlank()) {
                        config.setBcc(Arrays.stream(bccStr.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .toList());
                    }
                }
            }

            // Index / table name
            String index = getString(params, "index");
            if (index != null && !index.isBlank()) {
                config.setIndex(index.trim());
            }

            // Scheduling
            String frequency = getString(params, "frequency");
            if (frequency != null && !frequency.isBlank()) {
                config.setFrequency(frequency.toLowerCase().trim());
                config.setSchedulerType("CRON");

                // If cron is not explicitly given, derive it from frequency
                String cron = getString(params, "cron");
                if (cron != null && !cron.isBlank()) {
                    config.setCron(normalizeCron(cron.trim()));
                } else {
                    config.setCron(frequencyToCron(config.getFrequency()));
                }
            }

            ReportConfig saved = configService.saveConfig(config);

            log.info(
                    "Created new report config: {} (db={}, type={}, file={}, publisher={})",
                    saved.getReportName(),
                    saved.getDbType(),
                    saved.getReportType(),
                    saved.getFileType(),
                    saved.getPublisher()
            );

            Map<String, Object> data = new HashMap<>();
            data.put("reportName", saved.getReportName());
            data.put("reportConfig", saved);

            return ToolResponse.builder()
                    .status("SUCCESS")
                    .message("Report configuration created successfully: " + saved.getReportName())
                    .data(data)
                    .build();

        } catch (Exception ex) {

            log.error("Failed to create report config", ex);

            return ToolResponse.builder()
                    .status("FAILED")
                    .message("Failed to create report configuration: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * Safely extracts a String parameter from the params map.
     */
    private String getString(Map<String, Object> params, String key) {
        Object val = params.get(key);
        return val != null ? val.toString() : null;
    }

    /**
     * Converts a frequency keyword to a cron expression.
     * daily   → every day at 8am
     * weekly  → every Monday at 8am
     * monthly → 1st of month at 8am
     */
    private String frequencyToCron(String frequency) {
        return switch (frequency.toLowerCase()) {
            case "daily"   -> "0 0 8 * * *";
            case "weekly"  -> "0 0 8 * * 1";
            case "monthly" -> "0 0 8 1 * *";
            case "hourly"  -> "0 0 * * * *";
            case "minute"  -> "0 * * * * *";
            default        -> "0 0 8 * * *";
        };
    }

    /**
     * Normalizes a cron expression to ensure it has 6 fields for Spring CronTrigger.
     * If the LLM generates a 5-field Unix cron (e.g. '* * * * *'), prepends '0 '.
     */
    private String normalizeCron(String cron) {
        if (cron == null || cron.isBlank()) return cron;
        String[] parts = cron.trim().split("\\s+");
        if (parts.length == 5) {
            return "0 " + cron.trim();
        }
        return cron.trim();
    }
}
