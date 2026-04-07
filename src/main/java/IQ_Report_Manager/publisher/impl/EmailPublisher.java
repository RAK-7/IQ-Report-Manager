package IQ_Report_Manager.publisher.impl;

import IQ_Report_Manager.dto.ReportData;
import IQ_Report_Manager.factory.filehandler.FileHandlerFactory;
import IQ_Report_Manager.filehandler.FileHandler;
import IQ_Report_Manager.generator.impl.ReportGenerator;
import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.publisher.Publisher;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EmailPublisher implements Publisher {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ReportGenerator reportGenerator;

    @Autowired
    private FileHandlerFactory fileHandlerFactory;

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

            // Generate report data
            ReportData reportData = reportGenerator.generate(config, data);

            FileHandler handler = fileHandlerFactory.getHandler(config.getFileType());

            byte[] file = handler.generate(reportData);

            helper.addAttachment(
                    "report." + config.getFileType().toLowerCase(),
                    new ByteArrayResource(file)
            );

            mailSender.send(mimeMessage);

            System.out.println("Mail Sent Successfully");

        } catch (Exception e) {
            log.error("Error while sending email report", e);
            throw new RuntimeException("Failed to send report email", e);
        }
    }
}