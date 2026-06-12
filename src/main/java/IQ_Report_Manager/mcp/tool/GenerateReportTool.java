package IQ_Report_Manager.mcp.tool;

import IQ_Report_Manager.ai.executor.ReportExecutionResult;
import IQ_Report_Manager.mcp.dto.ToolRequest;
import IQ_Report_Manager.mcp.dto.ToolResponse;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import IQ_Report_Manager.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Tool responsible for generating reports.
 *
 * Flow:
 *
 * find_report_config
 *          ↓
 * generate_report
 *          ↓
 * ReportExecutionResult
 *          ↓
 * publish_report
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateReportTool implements McpTool {

    private final ConfigService configService;

    private final ReportService reportService;

    @Override
    public String getName() {

        return "generate_report";
    }

    @Override
    public ToolMetadata getMetadata() {

        ToolMetadata metadata =
                ToolMetadata.builder()
                        .toolName("generate_report")
                        .description(
                                "Generate report using existing ReportConfig"
                        )
                        .build();

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportName")
                        .type("String")
                        .description(
                                "Existing report configuration name"
                        )
                        .required(true)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("fileType")
                        .type("String")
                        .description(
                                "CSV or XLSX"
                        )
                        .required(false)
                        .build()
        );

        metadata.addParameter(
                ToolSchema.builder()
                        .name("reportType")
                        .type("String")
                        .description(
                                "RAW or AGG — overrides the stored config. Use AGG for aggregate reports, RAW for raw data."
                        )
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

            Map<String, Object> params =
                    request.getParameters();

            if (params == null) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message("Parameters are required")
                        .build();
            }

            String reportName =
                    (String) params.get("reportName");

            if (reportName == null
                    || reportName.isBlank()) {

                List<ReportConfig> configs = configService.getAllConfigs();
                if (configs != null && !configs.isEmpty()) {
                    reportName = configs.get(0).getReportName();
                    log.warn("reportName missing in generate_report. Using fallback: {}", reportName);
                } else {
                    return ToolResponse.builder()
                            .status("FAILED")
                            .message("reportName is required and no configurations exist")
                            .build();
                }
            }

            log.info(
                    "Generating report: {}",
                    reportName
            );

            /**
             * Load ReportConfig.
             */
            ReportConfig config =
                    configService.getConfigByName(
                            reportName
                    );

            if (config == null) {

                return ToolResponse.builder()
                        .status("FAILED")
                        .message(
                                "Report configuration not found"
                        )
                        .build();
            }

            /**
             * Runtime overrides — user's request takes priority over stored config.
             *
             * fileType override: CSV / XLSX
             */
            if (params.containsKey("fileType") && params.get("fileType") != null) {

                String requestedFileType = params.get("fileType").toString().toUpperCase().trim();
                config.setFileType(requestedFileType);

                log.info("fileType override applied: {}", requestedFileType);
            }

            /**
             * reportType override: RAW / AGG.
             *
             * Normalizes common LLM variants:
             * "aggregate", "AGGREGATE", "agg", "AGG" → "AGG"
             * "raw", "RAW"                           → "RAW"
             *
             * This ensures "Generate an aggregate report" always
             * produces AGG even if the stored config says RAW.
             */
            if (params.containsKey("reportType") && params.get("reportType") != null) {

                String requestedType = params.get("reportType").toString().toUpperCase().trim();

                // Normalize: AGGREGATE / AGGREGATED / AGG all → "AGG"
                if (requestedType.startsWith("AGG") || requestedType.equals("AGGREGATE")) {
                    requestedType = "AGG";
                } else if (requestedType.equals("RAW")) {
                    requestedType = "RAW";
                }

                config.setReportType(requestedType);
                log.info("reportType override applied: {}", requestedType);
            }

            /**
             * Start execution timer.
             */
            LocalDateTime startTime =
                    LocalDateTime.now();

            /**
             * Actual report generation.
             */
            ReportExecutionResult executionResult =
                    reportService.generateReportWithResult(
                            config
                    );

            LocalDateTime endTime =
                    LocalDateTime.now();

//            /**
//             * Generated file name.
//             *
//             * NOTE:
//             * Later we'll get actual filename
//             * from ReportService.
//             */
//            String fileName =
//                    config.getReportName()
//                            + "_"
//                            + System.currentTimeMillis()
//                            + "."
//                            + config.getFileType()
//                            .toLowerCase();
//
//            String filePath =
//                    "reports/" + fileName;
//
//            /**
//             * Build execution result.
//             */
//            ReportExecutionResult executionResult =
//                    ReportExecutionResult.builder()
//                            .reportName(
//                                    config.getReportName()
//                            )
//                            .fileName(
//                                    fileName
//                            )
//                            .filePath(
//                                    filePath
//                            )
//                            .startTime(
//                                    startTime
//                            )
//                            .endTime(
//                                    endTime
//                            )
//                            .reportType(
//                                    config.getReportType()
//                            )
//                            .dbType(
//                                    config.getDbType()
//                            )
//                            .fileType(
//                                    config.getFileType()
//                            )
//                            .reportConfig(
//                                    config
//                            )
//                            .status(
//                                    "SUCCESS"
//                            )
//                            .build();

            /**
             * Tool output.
             */
            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "executionResult",
                    executionResult
            );

            return ToolResponse.builder()
                    .status("SUCCESS")
                    .message(
                            "Report generated successfully"
                    )
                    .data(data)
                    .build();

        } catch (Exception ex) {

            log.error(
                    "Error generating report",
                    ex
            );

            return ToolResponse.builder()
                    .status("FAILED")
                    .message(ex.getMessage())
                    .build();
        }
    }
}