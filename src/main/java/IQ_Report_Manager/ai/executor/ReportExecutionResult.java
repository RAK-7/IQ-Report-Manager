package IQ_Report_Manager.ai.executor;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents the output of a report generation.
 *
 * This object is passed between tools.
 *
 * Example:
 *
 * generate_report
 *      ↓
 * ReportExecutionResult
 *      ↓
 * publish_report
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportExecutionResult {

    /**
     * Report name.
     */
    private String reportName;

    /**
     * Generated file name.
     */
    private String fileName;

    /**
     * Generated file path.
     */
    private String filePath;

    /**
     * Number of rows exported.
     */
    private Long recordCount;

    /**
     * Report execution start time.
     */
    private LocalDateTime startTime;

    /**
     * Report execution end time.
     */
    private LocalDateTime endTime;

    /**
     * Config used to generate report.
     */
    private ReportConfig reportConfig;

    /**
     * Output format.
     */
    private String fileType;

    /**
     * RAW or AGG.
     */
    private String reportType;

    /**
     * MYSQL or ELASTICSEARCH.
     */
    private String dbType;

    /**
     * SUCCESS / FAILED.
     */
    private String status;

}