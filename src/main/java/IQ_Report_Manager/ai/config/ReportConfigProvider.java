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
                    No report configurations available.
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

        return sb.toString();
    }
}