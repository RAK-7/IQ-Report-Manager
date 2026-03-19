package IQ_Report_Manager.factory.data;

import IQ_Report_Manager.repository.data.DataRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataRepositoryFactory {

    private final Map<String, DataRepository> repositoryMap = new HashMap<>();

    public DataRepositoryFactory(List<DataRepository> repositories) {

        for (DataRepository repo : repositories) {
            repositoryMap.put(repo.getDbType(), repo);
        }
    }

    public DataRepository getRepository(String dbType) {

        DataRepository repo = repositoryMap.get(dbType);

        if (repo == null) {
            throw new RuntimeException("No repository found for DB type: " + dbType);
        }

        return repo;
    }
}
