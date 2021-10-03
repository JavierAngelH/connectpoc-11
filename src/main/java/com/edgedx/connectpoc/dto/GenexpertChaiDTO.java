package com.edgedx.connectpoc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GenexpertChaiDTO {

    private String password;
    
    private String systemName;
    
    private String exportedDate;

    private String assay;
    
    private String assayVersion;

    private String sampleId;
    
    private String patientId;

    private String user;
    
    private String status;

    private String startTime;

    private String endTime;

    private String errorStatus;

    private String reagentLotId;

    private String cartridgeExpirationDate;
    
    private String cartridgeSerial;

    private String moduleName;

    private String moduleSerial;

    private String instrumentSerial;

    private String softwareVersion;

    private String resultId;

    private String resultIdinterpretation;

    private String deviceSerial;
    
    @JsonIgnore
    private Long id;

}
