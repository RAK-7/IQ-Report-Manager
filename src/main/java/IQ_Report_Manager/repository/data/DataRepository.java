package IQ_Report_Manager.repository.data;


import IQ_Report_Manager.model.config.mongo.ReportConfig;
import java.util.List;
import java.util.Map;


    public interface DataRepository {

        String getDbType();

        List<Map<String, Object>> fetchData(ReportConfig config);

    }

