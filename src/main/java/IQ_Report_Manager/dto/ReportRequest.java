package IQ_Report_Manager.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ReportRequest {

    private String dbType;

    private Map<String, String> mapping;

    private String reportType;

    private String publisher;

    private String reportName;

    private Map<String, List<String>> publisherConfig;

    private String cron;

    private String triggerTime;
}
