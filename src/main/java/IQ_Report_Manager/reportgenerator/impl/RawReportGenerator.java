package IQ_Report_Manager.reportgenerator.impl;

import IQ_Report_Manager.reportgenerator.ReportGenerator;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.util.SpelUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RawReportGenerator implements ReportGenerator {

    private final SpelUtil spelUtil;

    @Override
    public Map<String, Object> processRow(Map<String, Object> sourceRow, ReportConfig config) {

        Map<String, String> mapping = config.getMapping();
        Map<String, Object> reportRow = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : mapping.entrySet()) {

            String reportField = entry.getKey();
            String sourceField = entry.getValue();

            String value;

            try {
                if (sourceField.startsWith("#")) {
                    //  SpEL support
                    value = clean(spelUtil.evaluate(sourceField, sourceRow));
                } else {
                    //  Safe extraction
                    value = clean(getSafe(sourceRow.get(sourceField)));
                }
            } catch (Exception e) {
                value = "";
            }

            reportRow.put(reportField, value);
        }

        return reportRow;
    }

    private String getSafe(Object value) {
        return value != null ? value.toString() : "";
    }

    private String clean(String value) {
        return value == null ? "" : value.replace(",", " ").replace("\n", " ");
    }
}