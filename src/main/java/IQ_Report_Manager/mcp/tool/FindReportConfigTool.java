package IQ_Report_Manager.mcp.tool;

import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FindReportConfigTool implements McpTool {

    private final ConfigService configService;

    @Override
    public String getName() {
        return "find_report_config";
    }

    @Override
    public ToolMetadata getMetadata() {

        ToolMetadata metadata =
                ToolMetadata.builder()
                        .toolName("find_report_config")
                        .description(
                                "Find the closest matching report configuration"
                        )
                        .build();

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportName")
                        .type("String")
                        .description(
                                "User requested report name"
                        )
                        .required(true)
                        .build()
        );

        return metadata;
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        try {

            if (request.getParameters() == null) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message("Parameters are required")
                        .build();
            }

            Object reportNameObj =
                    request.getParameters() != null
                            ? request.getParameters().get("reportName")
                            : null;

            /*
             * reportName may be null if the LLM didn't pass it (small model limitation).
             * In that case, list all configs and pick the best match or return all.
             */
            String reportName = reportNameObj != null
                    ? reportNameObj.toString().toLowerCase().trim()
                    : null;

            List<ReportConfig> configs = configService.getAllConfigs();

            if (configs == null || configs.isEmpty()) {

                /*
                 * Return NOT_FOUND (not FAILED) so PlanExecutor
                 * continues to create_report_config if needed.
                 */
                return ToolResponse.builder()
                        .status("NOT_FOUND")
                        .message("No report configurations found. Create one first.")
                        .build();
            }

            /*
             * If no reportName given, return the first config as a fallback.
             */
            if (reportName == null || reportName.isBlank()) {

                log.warn("reportName not provided — returning first available config as fallback");

                ReportConfig fallback = configs.get(0);
                Map<String, Object> data = new HashMap<>();
                data.put("reportConfig", fallback);
                data.put("reportName", fallback.getReportName());
                data.put("score", 0);
                data.put("confidence", "LOW");

                return ToolResponse.builder()
                        .status("SUCCESS")
                        .message("Returned first available config (reportName not specified)")
                        .data(data)
                        .build();
            }

            ConfigMatchResult bestMatch = null;

            String[] keywords =
                    reportName.split("\\s+");

            for (ReportConfig config : configs) {

                int score =
                        calculateScore(
                                reportName,
                                config
                        );

                log.info(
                        "Config={} Score={}",
                        config.getReportName(),
                        score
                );

                if (bestMatch == null
                        || score > bestMatch.getScore()) {

                    bestMatch =
                            ConfigMatchResult.builder()
                                    .config(config)
                                    .score(score)
                                    .confidence(
                                            confidence(score)
                                    )
                                    .build();
                }
            }

            if (bestMatch == null) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message(
                                "No matching report configuration found"
                        )
                        .build();
            }

            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "reportConfig",
                    bestMatch.getConfig()
            );

            data.put(
                    "reportName",
                    bestMatch.getConfig().getReportName()
            );

            data.put(
                    "score",
                    bestMatch.getScore()
            );

            data.put(
                    "confidence",
                    bestMatch.getConfidence()
            );

            return ToolResponse.builder()
                    .status("SUCCESS")
                    .message(
                            "Matching report configuration found"
                    )
                    .data(data)
                    .build();

        } catch (Exception ex) {

            return ToolResponse.builder()
                    .status("FAILED")
                    .message(ex.getMessage())
                    .build();
        }
    }
    private int calculateScore(
            String request,
            ReportConfig config
    ) {

        int score = 0;

        String normalizedRequest =
                request.toLowerCase();

        /*
         * Report Name
         */
        if (config.getReportName() != null) {

            String reportName =
                    config.getReportName()
                            .toLowerCase();

            for (String keyword :
                    normalizedRequest.split("\\s+")) {

                if (reportName.contains(keyword)) {

                    score += 10;
                }
            }
        }

        /*
         * Report Type
         */
        if (config.getReportType() != null) {

            String reportType =
                    config.getReportType()
                            .toLowerCase();

            if (normalizedRequest.contains("agg")
                    || normalizedRequest.contains("aggregate")) {

                if ("agg".equals(reportType)) {

                    score += 25;
                }
            }

            if (normalizedRequest.contains("raw")) {

                if ("raw".equals(reportType)) {

                    score += 25;
                }
            }
        }

        /*
         * File Type
         */
        if (config.getFileType() != null) {

            String fileType =
                    config.getFileType()
                            .toLowerCase();

            if (normalizedRequest.contains("xlsx")
                    || normalizedRequest.contains("excel")) {

                if ("xlsx".equals(fileType)) {

                    score += 15;
                }
            }

            if (normalizedRequest.contains("csv")) {

                if ("csv".equals(fileType)) {

                    score += 15;
                }
            }
        }

        /*
         * DB Type
         */
        if (config.getDbType() != null) {

            String dbType =
                    config.getDbType()
                            .toLowerCase();

            if (normalizedRequest.contains("elastic")) {

                if (dbType.contains("elastic")) {

                    score += 10;
                }
            }

            if (normalizedRequest.contains("mysql")) {

                if (dbType.contains("mysql")) {

                    score += 10;
                }
            }
        }

        /*
         * Index
         */
        if (config.getIndex() != null) {

            String index =
                    config.getIndex()
                            .toLowerCase();

            for (String keyword :
                    normalizedRequest.split("\\s+")) {

                if (index.contains(keyword)) {

                    score += 5;
                }
            }
        }

        return score;
    }
    private String confidence(
            int score
    ) {

        if (score >= 60) {
            return "HIGH";
        }

        if (score >= 30) {
            return "MEDIUM";
        }

        return "LOW";
    }
}