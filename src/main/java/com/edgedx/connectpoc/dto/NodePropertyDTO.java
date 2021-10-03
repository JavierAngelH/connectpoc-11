package com.edgedx.connectpoc.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NodePropertyDTO {

    private String name;
    private String value;
    private Boolean update;

}
