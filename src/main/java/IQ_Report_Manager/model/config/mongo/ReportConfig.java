package IQ_Report_Manager.model.config.mongo;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Data
@Controller
@Document(collection = "report_configs")
public class ReportConfig {

    @Id
    private String id;

    private String index;

    private String email;

    private List<String> cc;
    private List<String> bcc;

    private String dbType;

    private String schedulerType;

    private Map<String, String> mapping;

    private String reportType;

    private String publisher;

    private String reportName;

    private Map<String, List<String>> publisherConfig;

    private String cron;

    private long triggerTime;

    public String FileType;

}
