package IQ_Report_Manager.model.data.elastic;


import lombok.Data;

@Data
public class SystemMetaData {
    private String bindId;
    private String countryCode;
    private String startTime;
    private long deliverTime;
    private long submitTime;
    private String countryName;

    private OperatorDetails operatorDetails;

}
