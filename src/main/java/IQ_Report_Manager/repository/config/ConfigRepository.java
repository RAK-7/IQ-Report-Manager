package IQ_Report_Manager.repository.config;

import IQ_Report_Manager.model.config.mongo.ReportConfig;

import java.util.List;

public interface ConfigRepository {

    String getConfigType();
    List<ReportConfig> getAllConfigs();
    ReportConfig saveConfig(ReportConfig Config);
    ReportConfig getConfigById(String id);

}
