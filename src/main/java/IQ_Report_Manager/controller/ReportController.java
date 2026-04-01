package IQ_Report_Manager.controller;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.service.ConfigService;
import IQ_Report_Manager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final ConfigService configService;
    private final ReportService reportService;

    // Create new report configuration
    @PostMapping("/config")
    public ReportConfig createConfig(@RequestBody ReportConfig config) {
        return configService.saveConfig(config);
    }

    // Get all report configs
    @GetMapping("/configs")
    public List<ReportConfig> getConfigs() {
        return configService.getAllConfigs();
    }

    @PostMapping("/run/{id}")
    public List<Map<String, Object>> runReport(@PathVariable String id) {
        ReportConfig config = configService.getConfigById(id);
        return reportService.generateReport(config);
    }
}