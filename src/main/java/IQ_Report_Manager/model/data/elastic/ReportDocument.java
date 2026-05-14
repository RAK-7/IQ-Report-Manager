package IQ_Report_Manager.model.data.elastic;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.util.Date;

@Data
@Document(indexName = "reports")
public class ReportDocument {

    @Id
    private String messageId;
    private String customerId;
    private String headerId;
    private String message;
    private String mobileNum;
    private String status;
    private int wordcount;
    private int multipartSize;
    @Field(type = FieldType.Date)
    private java.util.Date timestamp;
    private TemplateDetails templateDetails;
    private SystemMetaData systemMetaData;
    private Metadata metadata;

}
