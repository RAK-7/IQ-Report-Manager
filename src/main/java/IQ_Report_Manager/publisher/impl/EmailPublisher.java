package IQ_Report_Manager.publisher.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.publisher.Publisher;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Publishes reports by sending them as email attachments.
 *
 * Features:
 * - Configurable from-address via application.properties
 * - Dynamic subject line (includes report name)
 * - HTML email body with report metadata
 * - CC and BCC support
 * - Proper email validation
 */
@Component
@Slf4j
public class EmailPublisher implements Publisher {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public String getPublisherType() {
        return "Email";
    }

    @Override
    public void publish(ReportConfig config, String fileName) {

        try {

            validateConfig(config, fileName);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // From
            helper.setFrom(fromAddress);

            // To
            helper.setTo(config.getEmail().trim());

            // CC
            if (config.getCc() != null) {
                List<String> validCc = config.getCc().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

                for (String cc : validCc) {
                    if (!cc.contains("@")) {
                        throw new IllegalArgumentException("Invalid CC email address: " + cc);
                    }
                }

                if (!validCc.isEmpty()) {
                    helper.setCc(validCc.toArray(new String[0]));
                }
            }

            // BCC
            if (config.getBcc() != null) {
                List<String> validBcc = config.getBcc().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

                for (String bcc : validBcc) {
                    if (!bcc.contains("@")) {
                        throw new IllegalArgumentException("Invalid BCC email address: " + bcc);
                    }
                }

                if (!validBcc.isEmpty()) {
                    helper.setBcc(validBcc.toArray(new String[0]));
                }
            }

            // Subject — dynamic with report name
            String subject = buildSubject(config);
            helper.setSubject(subject);

            // HTML body with report metadata
            String body = buildHtmlBody(config, fileName);
            helper.setText(body, true); // true = HTML

            // Attachment — the report file
            File reportFile = new File(fileName);

            if (!reportFile.exists()) {
                throw new RuntimeException(
                        "Report file not found: " + fileName
                );
            }

            helper.addAttachment(
                    reportFile.getName(),
                    new FileSystemResource(reportFile)
            );

            mailSender.send(mimeMessage);

            log.info(
                    "Report '{}' emailed successfully to {} (file: {})",
                    config.getReportName(),
                    config.getEmail(),
                    fileName
            );

        } catch (Exception e) {
            log.error("Error while sending email report '{}': {}", config.getReportName(), e.getMessage(), e);
            throw new RuntimeException("Failed to send report email: " + e.getMessage(), e);
        }
    }

    /**
     * Validates that required email config fields are present.
     */
    private void validateConfig(ReportConfig config, String fileName) {

        if (config == null) {
            throw new IllegalArgumentException("ReportConfig cannot be null");
        }

        if (config.getEmail() == null || config.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "No email recipient configured for report: " + config.getReportName()
            );
        }

        if (!config.getEmail().contains("@")) {
            throw new IllegalArgumentException(
                    "Invalid email address: " + config.getEmail()
            );
        }

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
    }

    /**
     * Builds a descriptive subject line.
     * Example: "IQ Report Manager — Sales Report (XLSX) | 10 Jun 2026"
     */
    private String buildSubject(ReportConfig config) {

        String reportName = config.getReportName() != null
                ? capitalize(config.getReportName())
                : "Report";

        String fileType = config.getFileType() != null
                ? config.getFileType().toUpperCase()
                : "";

        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

        if (fileType.isBlank()) {
            return "IQ Report Manager — " + reportName + " | " + date;
        }

        return "IQ Report Manager — " + reportName + " (" + fileType + ") | " + date;
    }

    /**
     * Builds a clean HTML email body with report metadata.
     */
    private String buildHtmlBody(ReportConfig config, String fileName) {

        String reportName = config.getReportName() != null
                ? capitalize(config.getReportName())
                : "Report";

        String dateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        String dbType    = config.getDbType()    != null ? config.getDbType()    : "-";
        String repType   = config.getReportType() != null ? config.getReportType() : "-";
        String fileType  = config.getFileType()  != null ? config.getFileType().toUpperCase()  : "-";

        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; padding: 20px;">
                
                  <h2 style="color: #2c3e50;">📊 IQ Report Manager</h2>
                  
                  <p>Hello,</p>
                  
                  <p>Your report <strong>%s</strong> has been generated and is attached to this email.</p>
                  
                  <table style="border-collapse: collapse; width: 100%%; max-width: 500px; margin: 16px 0;">
                    <tr style="background-color: #f8f9fa;">
                      <td style="padding: 8px 12px; border: 1px solid #dee2e6; font-weight: bold;">Report Name</td>
                      <td style="padding: 8px 12px; border: 1px solid #dee2e6;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding: 8px 12px; border: 1px solid #dee2e6; font-weight: bold;">Report Type</td>
                      <td style="padding: 8px 12px; border: 1px solid #dee2e6;">%s</td>
                    </tr>
                    <tr style="background-color: #f8f9fa;">
                      <td style="padding: 8px 12px; border: 1px solid #dee2e6; font-weight: bold;">File Format</td>
                      <td style="padding: 8px 12px; border: 1px solid #dee2e6;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding: 8px 12px; border: 1px solid #dee2e6; font-weight: bold;">Data Source</td>
                      <td style="padding: 8px 12px; border: 1px solid #dee2e6;">%s</td>
                    </tr>
                    <tr style="background-color: #f8f9fa;">
                      <td style="padding: 8px 12px; border: 1px solid #dee2e6; font-weight: bold;">Generated At</td>
                      <td style="padding: 8px 12px; border: 1px solid #dee2e6;">%s</td>
                    </tr>
                  </table>
                  
                  <p style="color: #6c757d; font-size: 12px;">
                    This email was sent automatically by IQ Report Manager.<br>
                    Please do not reply to this email.
                  </p>
                
                </body>
                </html>
                """.formatted(
                        reportName,
                        reportName,
                        repType,
                        fileType,
                        dbType,
                        dateTime
                );
    }

    /**
     * Capitalizes each word in a string.
     * "sales_report" → "Sales Report"
     */
    private String capitalize(String input) {
        if (input == null || input.isBlank()) return input;
        return java.util.Arrays.stream(
                        input.replace("_", " ").split("\\s+")
                )
                .filter(w -> !w.isBlank())
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase())
                .collect(java.util.stream.Collectors.joining(" "));
    }
}