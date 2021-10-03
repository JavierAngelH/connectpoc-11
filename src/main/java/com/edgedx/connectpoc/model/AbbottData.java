package com.edgedx.connectpoc.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AbbottData {

    private String reagentLotId;    
    private String reagentSerialNumber;
    private String controlLotNumber;
    private String resultType;
    private String value;
    private String units;
    private String referenceRanges;
    private String operatorIdentification;
    private LocalDateTime datetimeCompleteTest;

    
}
