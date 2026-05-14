package IQ_Report_Manager.publisher.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.publisher.Publisher;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class EmailPublisher implements Publisher {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public String getPublisherType() {
        return "Email";
    }

    @Override
    public void publish(ReportConfig config, String fileName) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("rahulkarn77777@gmail.com");
            helper.setTo(config.getEmail());

            if (config != null) {

                // Handle CC
                if (config.getCc() != null) {
                    List<String> validCc = config.getCc().stream()
                            .filter(Objects::nonNull)          // remove null values
                            .map(String::trim)                 // remove leading/trailing spaces
                            .filter(s -> !s.isEmpty())         // remove empty strings
                            .filter(s -> s.contains("@"))      // basic email validation
                            .toList();

                    if (!validCc.isEmpty()) {
                        helper.setCc(validCc.toArray(new String[0]));
                    }
                }

                // Handle BCC
                if (config.getBcc() != null) {
                    List<String> validBcc = config.getBcc().stream()
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .filter(s -> s.contains("@"))
                            .toList();

                    if (!validBcc.isEmpty()) {
                        helper.setBcc(validBcc.toArray(new String[0]));
                    }
                }
            }

            helper.setSubject("Report Manager");
            helper.setText("Please find attached report");

            // ONLY attach file
            helper.addAttachment(
                    fileName,
                    new FileSystemResource(new File(fileName))
            );

            mailSender.send(mimeMessage);

            log.info("Mail sent successfully");

        } catch (Exception e) {
            log.error("Error while sending email report", e);
            throw new RuntimeException("Failed to send report email", e);
        }
    }
}