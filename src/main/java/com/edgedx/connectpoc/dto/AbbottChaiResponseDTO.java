package com.edgedx.connectpoc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AbbottChaiResponseDTO {

    @JsonProperty
    private String status;

    @JsonProperty
    private String message;

}
