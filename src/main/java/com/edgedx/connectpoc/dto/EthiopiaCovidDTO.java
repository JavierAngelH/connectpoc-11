package com.edgedx.connectpoc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EthiopiaCovidDTO {

    @JsonIgnore
    private Long id;

    private String specimenId;

    private String requestId;

    private String result;

    private String msg;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EthiopiaCovidDTO that = (EthiopiaCovidDTO) o;
        return Objects.equals(specimenId, that.specimenId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(specimenId);
    }
}
