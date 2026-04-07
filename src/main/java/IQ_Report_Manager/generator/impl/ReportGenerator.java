package IQ_Report_Manager.generator.impl;

import IQ_Report_Manager.dto.ReportData;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.util.SpelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
public class ReportGenerator {

    @Autowired
    private SpelUtil spelUtil;

    public ReportData generate(ReportConfig config, List<Map<String, Object>> data) {

        LinkedHashMap<String, Function<Map<String, Object>, String>> columnExtractors = new LinkedHashMap<>();

        //  use of spel
        config.getMapping().forEach((key, value) -> {
            columnExtractors.put(key, row -> {
                if (value.startsWith("#")) {
                    return clean(spelUtil.evaluate(value, row));
                } else {
                    return clean(getSafe(row.get(value)));
                }
            });
        });

        // Dynamic column removal
        List<String> finalColumns = new ArrayList<>();

        for (Map.Entry<String, Function<Map<String, Object>, String>> entry : columnExtractors.entrySet()) {

            boolean hasData = false;

            for (Map<String, Object> row : data) {
                String value = entry.getValue().apply(row);
                if (value != null && !value.trim().isEmpty()) {
                    hasData = true;
                    break;
                }
            }

            if (hasData) {
                finalColumns.add(entry.getKey());
            }
        }

        //  Build rows
        List<Map<String, String>> rows = new ArrayList<>();

        for (Map<String, Object> row : data) {

            Map<String, String> processedRow = new LinkedHashMap<>();

            for (String column : finalColumns) {
                String value = columnExtractors.get(column).apply(row);
                processedRow.put(column, value);
            }

            rows.add(processedRow);
        }

        return new ReportData(finalColumns, rows);
    }

    private String getSafe(Object value) {
        return value != null ? value.toString() : "";
    }

    private String clean(String value) {
        return value == null ? "" : value.replace(",", " ").replace("\n", " ");
    }
}