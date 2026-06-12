package IQ_Report_Manager.reportgenerator.impl;

import IQ_Report_Manager.reportgenerator.ReportGenerator;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.util.SpelUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RAW report generator.
 *
 * Behaviour:
 * - If config has a field mapping: apply it (rename/transform columns).
 * - If config has NO mapping (null or empty): pass all source columns through as-is.
 *   This is the default for dynamically-created configs where the user just says
 *   "give me the data from X table/index".
 */
@Component
@RequiredArgsConstructor
public class RawReportGenerator implements ReportGenerator {

    private final SpelUtil spelUtil;

    @Override
    public Map<String, Object> processRow(Map<String, Object> sourceRow, ReportConfig config) {

        Map<String, String> mapping = config.getMapping();

        /*
         * No mapping defined → pass all columns through directly.
         * This happens when a user creates a config on-the-fly without specifying mappings.
         */
        if (mapping == null || mapping.isEmpty()) {
            return passthroughRow(sourceRow);
        }

        /*
         * Mapping defined → apply field rename / SpEL transform.
         */
        Map<String, Object> reportRow = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : mapping.entrySet()) {

            String reportField = entry.getKey();
            String sourceField = entry.getValue();

            String value;

            try {
                if (sourceField.startsWith("#")) {
                    // SpEL expression support
                    value = clean(spelUtil.evaluate(sourceField, sourceRow));
                } else {
                    // Direct field extraction
                    value = clean(getSafe(sourceRow.get(sourceField)));
                }
            } catch (Exception e) {
                value = "";
            }

            reportRow.put(reportField, value);
        }

        return reportRow;
    }

    /**
     * Passes all source columns through with no transformation.
     * Used when no field mapping is configured.
     */
    private Map<String, Object> passthroughRow(Map<String, Object> sourceRow) {

        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : sourceRow.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            result.put(key, val != null ? clean(val.toString()) : "");
        }

        return result;
    }

    private String getSafe(Object value) {
        return value != null ? value.toString() : "";
    }

    private String clean(String value) {
        return value == null ? "" : value.replace(",", " ").replace("\n", " ");
    }
}