package com.edgedx.connectpoc.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HealthStatisticsChartResults {

    private Integer value;

    private LocalDateTime time;

}
