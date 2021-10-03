/** 
 * TriageGeneralData.java Created: Sep 6, 2018 JavierAngelH
 */

package com.edgedx.connectpoc.model;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TriageGeneralData {
    
    private Integer id;
    private String senderId;
    private LocalDateTime datetime;
    private String version;
    private String processId;
    private String patientId;
    private String labPatientId;
    private String InstrumentSerial;
    private String patientResultSerial;
    private String panelType;
    private String reagentLotNumber;
    private String priority;
    private String qcResultCode;
    private String patientResultApproval;
    private LocalDateTime resultDateTime;
    private String reportType;
    private String deviceId;
    private Set<TriageData> resultList;

}
