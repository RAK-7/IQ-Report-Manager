package IQ_Report_Manager.factory.data;

import IQ_Report_Manager.repository.data.DataRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory that resolves DataRepository by DB type.
 *
 * Supports:
 * - MYSQL
 * - ELASTICSEARCH (or ES as alias)
 * Case-insensitive lookup.
 */
@Component
public class DataRepositoryFactory {

    private final Map<String, DataRepository> repositoryMap = new HashMap<>();

    public DataRepositoryFactory(List<DataRepository> repositories) {

        for (DataRepository repo : repositories) {
            // Register by the canonical key
            repositoryMap.put(repo.getDbType().toUpperCase(), repo);
        }

        // Register aliases so both "ES" and "ELASTICSEARCH" work
        DataRepository esRepo = repositoryMap.get("ELASTICSEARCH");
        if (esRepo != null) {
            repositoryMap.putIfAbsent("ES", esRepo);
        }

        DataRepository mysqlRepo = repositoryMap.get("MYSQL");
        if (mysqlRepo != null) {
            repositoryMap.putIfAbsent("MYSQL", mysqlRepo);
        }
    }

    public DataRepository getRepository(String dbType) {

        if (dbType == null || dbType.isBlank()) {
            throw new RuntimeException("dbType cannot be null or empty");
        }

        DataRepository repo = repositoryMap.get(dbType.toUpperCase().trim());

        if (repo == null) {
            throw new RuntimeException(
                    "No repository found for DB type: '" + dbType
                            + "'. Available types: " + repositoryMap.keySet()
            );
        }

        return repo;
    }
}
