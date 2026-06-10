package IQ_Report_Manager.filehandler.impl;

import IQ_Report_Manager.filehandler.FileHandler;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CsvFileHandler implements FileHandler {

    private BufferedWriter writer;
    private boolean headerWritten = false;
    private String filePath;
    private long rowCount;

    @Override
    public String getType() {
        return "CSV";
    }

    @Override
    public void init(String fileName) throws Exception {
        this.filePath = fileName;
        this.rowCount = 0;
        writer = Files.newBufferedWriter(Paths.get(fileName));
        headerWritten = false;
    }

    @Override
    public void writeRow(Map<String, Object> row) {

        try {
            // Write header only once
            if (!headerWritten) {
                String header = String.join(",", row.keySet());
                writer.write(header);
                writer.newLine();
                headerWritten = true;
            }

            // Write row values
            String line = row.values().stream()
                    .map(value -> value != null ? value.toString() : "")
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            writer.write(line);
            writer.newLine();

        } catch (Exception e) {
            throw new RuntimeException("Error writing CSV row", e);
        }
        rowCount++;
    }

    @Override
    public void close() throws Exception {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }

    @Override
    public String getFilePath() {

        return filePath;
    }

    @Override
    public long getRowCount() {

        return rowCount;
    }
}