package IQ_Report_Manager.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class ReportData {

    private List<String> columns;
    private List<Map<String, String>> rows;

    public ReportData(List<String> columns, List<Map<String, String>> rows) {
        this.columns = columns;
        this.rows = rows;
    }
}