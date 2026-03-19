package IQ_Report_Manager.repository.config.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.repository.config.ConfigRepository;
import IQ_Report_Manager.repository.config.mongo.ReportConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MongoConfigRepoImpl implements ConfigRepository {

    @Autowired
    private ReportConfigRepository reportConfigRepository;

    @Override
    public String getConfigType() {
        return "MONGO";
    }

    @Override
    public List<ReportConfig> getAllConfigs() {
        return reportConfigRepository.findAll();
    }

    @Override
    public ReportConfig getConfigById(String id) {
        return reportConfigRepository.findById(id).orElse(null);
    }

    @Override
    public ReportConfig saveConfig(ReportConfig config) {
        return reportConfigRepository.save(config);
    }
}
