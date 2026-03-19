package IQ_Report_Manager.service;

import IQ_Report_Manager.factory.config.ConfigRepositoryFactory;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.repository.config.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigService {

    @Autowired
    ConfigRepository configRepository;

    @Autowired
    private ConfigRepositoryFactory configRepositoryFactory;

    public ReportConfig saveConfig(ReportConfig config) {

        ConfigRepository repo =
                configRepositoryFactory.getRepository("MONGO");

        return repo.saveConfig(config);
    }

    public List<ReportConfig> getAllConfigs() {

        ConfigRepository repo =
                configRepositoryFactory.getRepository("MONGO");

        return repo.getAllConfigs();
    }

    public ReportConfig getConfigById(String id) {
        return configRepository.getConfigById(id);
    }
}
