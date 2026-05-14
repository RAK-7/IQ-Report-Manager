package IQ_Report_Manager.reportgenerator.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.reportgenerator.ReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component("aggReportGenerator")
public class AggReportGenerator implements ReportGenerator {

    @Override
    public Map<String, Object> processRow(
            Map<String, Object> sourceRow,
            ReportConfig config
    ) {

        Map<String, Object> row = new LinkedHashMap<>();

        row.put("Date", sourceRow.get("Date"));
        row.put("Operator", sourceRow.get("Operator"));

        row.put(
                "Total Delivered",
                sourceRow.getOrDefault("Total Delivered", 0)
        );

        row.put(
                "Total Failed",
                sourceRow.getOrDefault("Total Failed", 0)
        );

        row.put(
                "Total Submitted",
                sourceRow.getOrDefault("Total Submitted", 0)
        );

        return row;
    }
}