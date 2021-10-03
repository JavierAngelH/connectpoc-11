package com.edgedx.connectpoc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TriageData {

    private String testId;
    private String dataValue;
    private String units;
    private String range;
    private String normalcyFlag;
    private String population;
    private String resultStatus;
    private String operatorId;

}
