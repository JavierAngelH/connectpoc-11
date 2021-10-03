package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
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
public class FacsprestoPatientSample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
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
    private String patientId;

    @Column(length = 250)
    private String instQcPassed;

    @Column(length = 250)
    private String reagentQcPassed;

    @Column(length = 250)
    private String cd4;

    private Double percentCd4;

    private Double hb;

    @Column(length = 250)
    private String errorCode;

    private Boolean isExported = false;

    private LocalDateTime sysLogdate = LocalDateTime.now();

    private Boolean apiExported = false;

    private LocalDateTime nascopTimestamp;

}