package com.edgedx.connectpoc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChaiFacsprestoSummaryDTO {

    @JsonProperty("Instrumentserialno")
    private String instrumentSerialNo;

    @JsonProperty("Laboratory")
    private String laboratory;

    @JsonProperty("month")
    private Integer month;

    @JsonProperty("year")
    private Integer year;

    @JsonProperty("totaltestsdone")
    private Integer totalTestsDone;

    @JsonProperty("catridgeused")
    private Integer cartridgeUsed;


}
