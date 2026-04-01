package IQ_Report_Manager.factory.config;

import IQ_Report_Manager.repository.config.ConfigRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ConfigRepositoryFactory {
    private static final Map<String, ConfigRepository> REPOSITORY_MAP = new HashMap<>();

    public ConfigRepositoryFactory(List<ConfigRepository> repositories) {
        for (ConfigRepository repo : repositories) {
            REPOSITORY_MAP.put(repo.getConfigType(), repo);
        }
    }

    public ConfigRepository getRepository(String configType) {
        ConfigRepository repo = REPOSITORY_MAP.get(configType);
        if (ObjectUtils.isEmpty(repo)) {
            throw new RuntimeException("No config repository found for type: " + configType);
        }

        return repo;
    }
}
