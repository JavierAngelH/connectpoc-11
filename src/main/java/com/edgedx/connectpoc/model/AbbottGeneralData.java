package com.edgedx.connectpoc.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AbbottGeneralData {

    private String senderName;
    private String version;
    private LocalDateTime datetime;
    private String sampleId;
    private Set<AbbottData> resultList;
    private String deviceSerialNumber;
    private String carrierId;
    private String position;
    private String assayNumber;
    private String assayName;
    private String reportType;
    private String error;

}
