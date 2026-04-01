package IQ_Report_Manager.publisher.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.publisher.Publisher;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

@Component
public class EmailPublisher implements Publisher {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public String getPublisherType() {
        return "Email";
    }

    @Override
    public void publish(ReportConfig config, List<Map<String, Object>> data) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("rahulkarn77777@gmail.com");
            helper.setTo(config.getEmail());

            if (config.getCc() != null && !config.getCc().isEmpty()) {
                helper.setCc(config.getCc().toArray(new String[0]));
            }

            if (config.getBcc() != null && !config.getBcc().isEmpty()) {
                helper.setBcc(config.getBcc().toArray(new String[0]));
            }

            helper.setSubject("Report Manager");
            helper.setText("Please find attached report");

            // SpelEvaluation

            // CSV LOGIC

            LinkedHashMap<String, Function<Map<String, Object>, String>> columnExtractors = new LinkedHashMap<>();
            config.getMapping().forEach((key, value) -> {
                    columnExtractors.put(value, row -> getSafe(row.get(value)));
            });

            columnExtractors.put("Time", this::getTime);
            columnExtractors.put("MsgId", row -> getSafe(row.get("messageId"))); // msgId
            columnExtractors.put("MobNum", row -> getSafe(row.get("mobileNum")));
            columnExtractors.put("Status", row -> getSafe(row.get("status")));
            columnExtractors.put("MultipartSize", row -> getSafe(row.get("multipartSize")));
            columnExtractors.put("Country", this::getCountry);
            columnExtractors.put("SubUserId", this::getSubUserId);
            columnExtractors.put("Operator", this::getOperator);
            columnExtractors.put("Msg", row -> getSafe(row.get("message")));

            List<String> finalColumns = new ArrayList<>();

            // Detect non-empty columns
            for (Map.Entry<String, Function<Map<String, Object>, String>> entry : columnExtractors.entrySet()) {

                boolean hasData = false;

                for (Map<String, Object> row : data) {
                    String value = entry.getValue().apply(row);
                    if (value != null && !value.trim().isEmpty()) {
                        hasData = true;
                        break;
                    }
                }

                if (hasData) {
                    finalColumns.add(entry.getKey());
                }
            }

            StringBuilder csvBuilder = new StringBuilder();

            // Header
            csvBuilder.append(String.join(",", finalColumns)).append("\n");

            // Rows
            for (Map<String, Object> row : data) {

                List<String> rowValues = new ArrayList<>();

                for (String column : finalColumns) {
                    String value = columnExtractors.get(column).apply(row);
                    rowValues.add(clean(value));
                }

                csvBuilder.append(String.join(",", rowValues)).append("\n");
            }

            //  ATTACH CSV
            helper.addAttachment(
                    "report.csv",
                    new ByteArrayResource(csvBuilder.toString().getBytes())
            );

            //  SEND MAIL
              mailSender.send(mimeMessage);

            System.out.println("Mail Sent Successfully");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //  HELPER METHODS

    private String getSafe(Object value) {
        return value != null ? value.toString() : "";
    }

    private String clean(String value) {
        return value == null ? "" : value.replace(",", " ").replace("\n", " ");
    }

    @SuppressWarnings("unchecked")
    private String getTime(Map<String, Object> row) {
        Map<String, Object> sys = (Map<String, Object>) row.get("systemMetaData");
        if (sys == null) return "";

        Object millisObj = sys.get("submitTime");
        if (millisObj == null) return "";

        long millis = Long.parseLong(millisObj.toString());

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(millis));
    }

    private String getCountry(Map<String, Object> row) {
        if (row.get("systemMetaData") instanceof Map<?, ?> sysMeta) {
            return getSafe(sysMeta.get("countryName"));
        }
        return "India";
    }

    @SuppressWarnings("unchecked")
    private String getOperator(Map<String, Object> row) {
        Map<String, Object> sys = (Map<String, Object>) row.get("systemMetaData");
        if (sys == null) return "";

        Map<String, Object> op = (Map<String, Object>) sys.get("operatorDetails");
        return op != null ? getSafe(op.get("operator")) : "";
    }

    @SuppressWarnings("unchecked")
    private String getSubUserId(Map<String, Object> row) {
        Map<String, Object> meta = (Map<String, Object>) row.get("metadata");
        return meta != null ? getSafe(meta.get("subUserId")) : "";
    }
}