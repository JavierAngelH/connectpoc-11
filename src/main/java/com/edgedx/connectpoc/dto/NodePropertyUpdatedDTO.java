package com.edgedx.connectpoc.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NodePropertyUpdatedDTO {

    private String nodeId;
    private String propertyName;
    private String propertyValue;

}
