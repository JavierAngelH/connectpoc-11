package com.edgedx.connectpoc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AbbottChaiDTO {

    @JsonProperty("sample_id")
    private String sampleId;

    @JsonProperty("test_datetime")
    private Timestamp testdatetime;

    @JsonProperty("result")
    private String results;

    @JsonProperty("units")
    private String units;

    @JsonProperty("test_error")
    private String testError;

    @JsonProperty("assay_name")
    private String assayName;

    @JsonProperty("carrier_id")
    private String carrierId;

    @JsonIgnore
    private Long id;

}
