package IQ_Report_Manager.repository.data;


import IQ_Report_Manager.filehandler.FileHandler;
import IQ_Report_Manager.model.config.mongo.ReportConfig;



    public interface DataRepository {

        String getDbType();

        void fetchData(ReportConfig config, FileHandler fileHandler);
    }

