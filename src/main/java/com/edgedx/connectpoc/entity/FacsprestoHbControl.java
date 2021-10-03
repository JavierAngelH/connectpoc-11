package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(indexes = {
        @Index(name = "UNIQUE_KEY", columnList = "runId, runDatetime", unique = true)
})
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FacsprestoHbControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(length = 250)
    private String version;

    @Column(length = 250)
    private String deviceCode;

    @Column(length = 250)
    private String labName;

    @Column(length = 250)
    private String runId;

    private LocalDateTime runDatetime;

    @Column(length = 250)
    private String operator;

    @Column(length = 250)
    private String reagentLotId;

    private LocalDate reagentLotExpDate;

    @Column(length = 250)
    private String processLotID;

    private LocalDate processLotExpDate;

    @Column(length = 250)
    private String level;

    private Double expHbLower;

    private Double expHbUpper;

    @Column( length = 250)
    private String reagentqcPperF;

    private Double hbgPerDl;

    @Column(length = 50)
    private String pass;

    @Column( length = 250)
    private String errorCode;

    private Boolean isExported = false;

    private LocalDateTime sysLogdate = LocalDateTime.now();

}