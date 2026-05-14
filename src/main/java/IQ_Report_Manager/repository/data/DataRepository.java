package IQ_Report_Manager.repository.data;


import IQ_Report_Manager.model.config.mongo.ReportConfig;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public interface DataRepository {

        String getDbType();

    void fetchData(ReportConfig config, Consumer<Map<String, Object>> consumer);

    List<Map<String, Object>> fetchAggData(ReportConfig config);
}

