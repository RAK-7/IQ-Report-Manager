package IQ_Report_Manager.factory.config;

import IQ_Report_Manager.repository.config.ConfigRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ConfigRepositoryFactory {
    private static final Map<String, ConfigRepository> REPOSITORY_MAP = new HashMap<>();

    public ConfigRepositoryFactory(List<ConfigRepository> repositories) {
        for (ConfigRepository repo : repositories) {
            REPOSITORY_MAP.put(repo.getConfigType(), repo);
        }
    }

    public ConfigRepository getRepository(String configType) {
        ConfigRepository repo = REPOSITORY_MAP.get(configType);

        if (repo == null) {
            throw new RuntimeException("No config repository found for type: " + configType);
        }

        return repo;
    }
}
