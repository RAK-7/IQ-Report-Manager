package IQ_Report_Manager.repository.data.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.model.data.mysql.ReportEntity;
import IQ_Report_Manager.repository.data.DataRepository;
import IQ_Report_Manager.repository.data.mysql.MysqlReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MysqlRepoImpl implements DataRepository {

    @Autowired
    private MysqlReportRepository mysqlReportRepository;

    @Override
    public String getDbType() {
        return "MYSQL";
    }

    @Override
    public List<Map<String, Object>> fetchData(ReportConfig config) {

        List<ReportEntity> entities = mysqlReportRepository.findAll();

        List<Map<String, Object>> results = new ArrayList<>();

        for (ReportEntity entity : entities) {

            Map<String, Object> map = new HashMap<>();

            map.put("timestamp", entity.getTimestamp());
            map.put("sourceAdd", entity.getSourceAdd());
            map.put("message", entity.getMessage());

            results.add(map);
        }

        return results;
    }
}
