package IQ_Report_Manager.repository.data.mysql;

import IQ_Report_Manager.model.data.mysql.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MysqlReportRepository extends JpaRepository<ReportEntity, Long> {
}
