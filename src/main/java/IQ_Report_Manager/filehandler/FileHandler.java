package IQ_Report_Manager.filehandler;

import IQ_Report_Manager.dto.ReportData;

public interface FileHandler {

    String getType();

    byte[] generate(ReportData reportData);
}