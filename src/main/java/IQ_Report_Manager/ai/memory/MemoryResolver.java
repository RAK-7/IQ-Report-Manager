package IQ_Report_Manager.ai.memory;

import org.springframework.stereotype.Component;

/**
 * Resolves references like:
 *
 * it
 * same report
 * previous report
 * that report
 */
@Component
public class MemoryResolver {

    public String resolve(
            String userMessage,
            MemoryContext memoryContext
    ) {

        if (memoryContext == null) {
            return userMessage;
        }

        String reportName =
                memoryContext.getLastReportName();

        if (reportName == null) {
            return userMessage;
        }

        String normalized =
                userMessage.toLowerCase();

        if (normalized.contains(" it ")) {

            return userMessage.replace(
                    "it",
                    reportName
            );
        }

        if (normalized.contains(
                "same report"
        )) {

            return userMessage.replace(
                    "same report",
                    reportName
            );
        }

        if (normalized.contains(
                "that report"
        )) {

            return userMessage.replace(
                    "that report",
                    reportName
            );
        }

        if (normalized.contains(
                "previous report"
        )) {

            return userMessage.replace(
                    "previous report",
                    reportName
            );
        }

        return userMessage;
    }
}