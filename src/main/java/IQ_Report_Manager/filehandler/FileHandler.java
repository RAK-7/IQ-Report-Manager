package IQ_Report_Manager.filehandler;

import java.util.Map;

public interface FileHandler {

    String getType();

    void init(String fileName) throws Exception;

    void writeRow(Map<String, Object> row) throws Exception;

    void close() throws Exception;
}