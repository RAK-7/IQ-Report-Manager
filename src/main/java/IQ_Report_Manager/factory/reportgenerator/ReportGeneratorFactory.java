package IQ_Report_Manager.factory.reportgenerator;

import IQ_Report_Manager.enums.ReportType;
import IQ_Report_Manager.reportgenerator.ReportGenerator;
import IQ_Report_Manager.reportgenerator.impl.RawReportGenerator;
import IQ_Report_Manager.reportgenerator.impl.AggReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportGeneratorFactory {

    private final RawReportGenerator rawReportGenerator;
//    private final AggReportGenerator aggregatedReportGenerator;

    public ReportGenerator getGenerator(String reportType) {

        ReportType type = ReportType.valueOf(reportType.toUpperCase());

        switch (type) {
            case RAW:
                return rawReportGenerator;

//            case AGG:
//                return aggregatedReportGenerator;

            default:
                throw new IllegalArgumentException("Invalid report type: " + reportType);
        }
    }
}