package IQ_Report_Manager.filehandler.impl;

import IQ_Report_Manager.filehandler.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.util.Map;

@Slf4j
@Component
public class XlsxFileHandler implements FileHandler {

    private SXSSFWorkbook workbook;
    private Sheet sheet;
    private int rowNum = 0;
    private String fileName;
    private boolean headerWritten = false;

    @Override
    public String getType() {
        return "XLSX";
    }

    @Override
    public void init(String fileName) throws Exception {
        this.fileName = fileName;

        // Streaming workbook (important for large data)
        this.workbook = new SXSSFWorkbook(100); // keeps 100 rows in memory
        this.sheet = workbook.createSheet("Report");

        log.info("Initialized XLSX file: {}", fileName);
    }

    @Override
    public void writeRow(Map<String, Object> data) throws Exception {

        // Write header only once
        if (!headerWritten) {
            Row headerRow = sheet.createRow(rowNum++);
            int col = 0;

            for (String key : data.keySet()) {
                Cell cell = headerRow.createCell(col++);
                cell.setCellValue(key);
            }

            headerWritten = true;
        }

        // Write data row
        Row row = sheet.createRow(rowNum++);
        int col = 0;

        for (Object value : data.values()) {
            Cell cell = row.createCell(col++);
            cell.setCellValue(value != null ? value.toString() : "");
        }
    }

    @Override
    public void close() throws Exception {

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            workbook.write(fos);
        } catch (Exception e) {
            log.error("Error writing XLSX file: {}", fileName, e);
            throw e;
        } finally {
            if (workbook != null) {
                workbook.dispose(); // very important for SXSSF
                workbook.close();
            }
        }

        log.info("XLSX file written successfully: {}", fileName);
    }
}