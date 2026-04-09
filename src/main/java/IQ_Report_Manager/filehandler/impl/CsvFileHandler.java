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

    @Override
    public String getType() {
        return "CSV";
    }

    @Override
    public void init(String fileName) throws Exception {
        writer = Files.newBufferedWriter(Paths.get(fileName));
    }

    @Override
    public void writeRow(Map<String, Object> row) throws Exception {

        String line = row.values().stream()
                .map(val -> val != null ? val.toString() : "")
                .collect(Collectors.joining(","));

        writer.write(line);
        writer.newLine();
    }

    @Override
    public void close() throws Exception {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }
}