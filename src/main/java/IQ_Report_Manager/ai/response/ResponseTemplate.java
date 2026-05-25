package IQ_Report_Manager.ai.response;

//Reusable response templates.


/**
 * Centralized response templates.
 */
public final class ResponseTemplate {

    private ResponseTemplate() {
    }

    public static final String SUCCESS =
            "The request was processed successfully.";

    public static final String FAILURE =
            "The request could not be completed.";

    public static final String CLARIFICATION =
            "Additional clarification is required.";

    public static final String REPORT_GENERATION =
            "The report will be generated and published.";

    public static final String SCHEDULING =
            "The report has been scheduled successfully.";
}
