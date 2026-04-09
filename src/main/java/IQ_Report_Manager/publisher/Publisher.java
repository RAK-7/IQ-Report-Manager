package IQ_Report_Manager.publisher;

import IQ_Report_Manager.model.config.mongo.ReportConfig;

import java.util.List;
import java.util.Map;

public interface Publisher {

    String getPublisherType();

    void publish(ReportConfig config, String fileName);
}
