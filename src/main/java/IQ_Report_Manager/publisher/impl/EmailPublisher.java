package IQ_Report_Manager.publisher.impl;

import IQ_Report_Manager.model.config.mongo.ReportConfig;
import IQ_Report_Manager.publisher.Publisher;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Component
public class EmailPublisher implements Publisher {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public String getPublisherType() {
        return "Email";
    }



    @Override
    public void publish(ReportConfig config, List<Map<String,Object>> data) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("rahulkarn77777@gmail.com");
            helper.setTo(config.getEmail());
            helper.setSubject("Report Manager");
            helper.setText("Please find attached report");

            // Create PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(){};
            PdfWriter.getInstance(document, baos);

            document.open();
            document.add(new Paragraph("Report Data\n\n"));

            if (data != null && !data.isEmpty()) {
                for (Map<String, Object> row : data) {
                    document.add(new Paragraph(row.toString()));
                }
            }

            document.close();

            // Attach PDF
            helper.addAttachment(
                    "report.pdf",
                    new ByteArrayResource(baos.toByteArray())
            );

            mailSender.send(mimeMessage);

            System.out.println("Mail Sent Successfully");

        } catch (Exception e) {
            System.out.println("EXCEPTION CLASS: " + e.getClass().getName());
            System.out.println("EXCEPTION MESSAGE: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}