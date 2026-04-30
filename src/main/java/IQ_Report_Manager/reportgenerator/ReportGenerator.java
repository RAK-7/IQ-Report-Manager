package IQ_Report_Manager.reportgenerator;

import IQ_Report_Manager.dto.ReportData;
import IQ_Report_Manager.model.config.mongo.ReportConfig;

import java.util.Map;

public interface ReportGenerator {

    Map<String, Object> processRow(Map<String, Object> sourceRow, ReportConfig config);

}
