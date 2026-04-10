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

            if (config.getCc() != null && !config.getCc().isEmpty()) {
                helper.setCc(config.getCc().toArray(new String[0]));
            }

            if (config.getBcc() != null && !config.getBcc().isEmpty()) {
                helper.setBcc(config.getBcc().toArray(new String[0]));
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