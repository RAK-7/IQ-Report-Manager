package IQ_Report_Manager.model.data.mysql;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "reports")
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String timestamp;

    private String sourceAdd;

    private String message;

}
