package com.edgedx.connectpoc.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChartResults {

    private Integer tests;

    private LocalDate date;
}
