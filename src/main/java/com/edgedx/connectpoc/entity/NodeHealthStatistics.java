
package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NodeHealthStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String macId;

    private LocalDateTime logDate;

    private String latitude;

    private String longitude;

    private Integer batteryStatus;

    private Integer networkSignalStrength;

    private Double temperature;

    private Double humidity;

    private Double ambientLight;

    private String networkOperator;

    private LocalDateTime sysLogDate = LocalDateTime.now();

    private Boolean isExported = false;


}
