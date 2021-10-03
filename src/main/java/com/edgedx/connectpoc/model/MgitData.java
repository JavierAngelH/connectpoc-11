/**
 * MgitData.java Created: Aug 14, 2017 JavierAngelH
 */

package com.edgedx.connectpoc.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MgitData {

    private Integer id;
    private String resultIdCode;
    private String testSequenceNumber;
    private String antibiotic;
    private Double concentration;
    private String concentrationUnits;
    private String testStatus;
    private Integer growthUnits;
    private String astSusceptibility;
    private LocalDateTime startDate;
    private LocalDateTime resultDate;
    private String instrumentType;
    private Integer protocolLength;
    private Integer instrumentNumber;
    private String instrumentLocation;
    private String preliminaryFinalStatus;

}
