package IQ_Report_Manager.model.data.elastic;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "reports")
public class ReportDocument {

    @Id
    private String id;

    private String timestamp;

    private String sourceAdd;

    private String message;

}
