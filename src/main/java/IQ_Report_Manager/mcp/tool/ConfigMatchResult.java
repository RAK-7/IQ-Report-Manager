package IQ_Report_Manager.mcp.tool;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import lombok.Builder;
import lombok.Data;

/**
 * Internal ranking result.
 */
@Data
@Builder
public class ConfigMatchResult {

    private ReportConfig config;

    private int score;

    private String confidence;
}