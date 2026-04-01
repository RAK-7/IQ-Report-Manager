package IQ_Report_Manager.service;

import IQ_Report_Manager.factory.config.ConfigRepositoryFactory;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.repository.config.ConfigRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigService {
    private final ConfigRepository configRepository;

    public ConfigService
            (ConfigRepositoryFactory configRepositoryFactory, @Value("${config.db.type}") String configDbType) {
        this.configRepository = configRepositoryFactory.getRepository(configDbType);
    }


    public ReportConfig saveConfig(ReportConfig config) {
        return configRepository.saveConfig(config);
    }

    public List<ReportConfig> getAllConfigs() {
        return configRepository.getAllConfigs();
    }

    public ReportConfig getConfigById(String id) {
        return configRepository.getConfigById(id);
    }
}
