package com.edgedx.connectpoc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChaiPatientSampleDTO {

    @JsonProperty("Instrumentserialno")
    private String instrumentserialno;

    @JsonProperty("Laboratory")
    private String laboratory;

    @JsonProperty("RunID")
    private String runId;

    @JsonProperty("RunDateTime")
    private String runDatetime;

    @JsonProperty("Operator")
    private String operator;

    @JsonProperty("ReagentLotID")
    private String reagentLotId;

    @JsonProperty("ReagentLotExp")
    private String reagentLotExp;

    @JsonProperty("ProcessLotID")
    private String processLotId;

    @JsonProperty("ProcessLotExp")
    private String processLotExp;

    @JsonProperty("PatientID")
    private String patientId;

    @JsonProperty("InstQCPassed")
    private String instQcPassed;

    @JsonProperty("ReagentQCPassed")
    private String reagentQcPassed;

    @JsonProperty("CD4result")
    private String cd4;

    @JsonProperty("CD4percent")
    private String percentCd4;

    @JsonProperty("Hb")
    private String hb;

    @JsonProperty("ErrorCodes")
    private String errorCode;

    @JsonIgnore
    private Long id;

}
