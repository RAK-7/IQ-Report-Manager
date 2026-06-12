package IQ_Report_Manager.ai.config;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides report configuration context
 * to the AI planner.
 *
 * Reads all available ReportConfigs
 * from MongoDB and converts them into
 * a prompt-friendly format.
 */
@Service
@RequiredArgsConstructor
public class ReportConfigProvider {

    private final ConfigService configService;

    /**
     * Builds configuration context
     * for LLM prompts.
     */
    public String buildConfigContext() {

        List<ReportConfig> configs =
                configService.getAllConfigs();

        if (configs == null || configs.isEmpty()) {

            return """
                    AVAILABLE REPORT CONFIGURATIONS
                    ====================================
                    NONE. The database is completely EMPTY.
                    
                    CRITICAL INSTRUCTION:
                    Because there are 0 configurations, you CANNOT use find_report_config, generate_report, or schedule_report without creating one first!
                    You MUST MUST MUST include "create_report_config" as the VERY FIRST step (order: 1) in your JSON plan, and populate ALL its parameters (reportName, dbType, reportType, etc.) based on the user's request.
                    """;
        }

        StringBuilder sb =
                new StringBuilder();

        sb.append("""
                
                AVAILABLE REPORT CONFIGURATIONS
                ====================================
                
                """);

        for (ReportConfig config : configs) {

            sb.append("Report Name : ")
                    .append(config.getReportName())
                    .append("\n");

            sb.append("DB Type : ")
                    .append(config.getDbType())
                    .append("\n");

            sb.append("Report Type : ")
                    .append(config.getReportType())
                    .append("\n");

            sb.append("File Type : ")
                    .append(config.getFileType())
                    .append("\n");

            sb.append("Publisher : ")
                    .append(config.getPublisher())
                    .append("\n");

            sb.append("Index : ")
                    .append(config.getIndex())
                    .append("\n");

            sb.append("-----------------------------------\n");
        }
        sb.append("""
                CRITICAL INSTRUCTION:
                If the user asks for a report name that is NOT EXACTLY listed above (e.g., they ask for a new report name), you MUST MUST MUST include "create_report_config" as the VERY FIRST step (order: 1) in your JSON plan! You cannot generate or schedule a report that does not exist in the list above!
                """);

        return sb.toString();
    }
}