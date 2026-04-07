package IQ_Report_Manager.filehandler.impl;

import IQ_Report_Manager.dto.ReportData;
import IQ_Report_Manager.filehandler.FileHandler;
import org.springframework.stereotype.Component;

@Component
public class CsvFileHandler implements FileHandler {

    @Override
    public String getType() {
        return "CSV";
    }

    @Override
    public byte[] generate(ReportData reportData) {

        StringBuilder csvBuilder = new StringBuilder();

        // Header
        csvBuilder.append(String.join(",", reportData.getColumns())).append("\n");

        // Rows
        reportData.getRows().forEach(row -> {
            reportData.getColumns().forEach(col -> {
                csvBuilder.append(row.getOrDefault(col, "")).append(",");
            });
            csvBuilder.append("\n");
        });

        return csvBuilder.toString().getBytes();
    }
}