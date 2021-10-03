package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PimaCd4 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "test_id")
    private Integer testId;

    @Column(name = "device_id", length = 45)
    private String deviceId;

    @Column(name = "assay_id", length = 45)
    private String assayId;

    @Column(name = "sample", length = 45)
    private String sample;

    @Column(name = "cd3cd4")
    private Integer cd3cd4;

    @Column(name = "error_message", length = 45)
    private String errorMessage;

    @Column(name = "operator", length = 45)
    private String operator;

    @Column(name = "result_date")
    private LocalDate resultDate;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "barcode", length = 45)
    private String barcode;

    @Column(name = "expiry_date", length = 45)
    private String expiryDate;

    @Column(name = "device", length = 45)
    private String device;

    @Column(name = "software_version", length = 45)
    private String softwareVersion;

    @Column(name = "volume", length = 45)
    private String volume;

    @Column(name = "reagent", length = 45)
    private String reagent;

    @Column(name = "assay_name", length = 45)
    private String assayName;

    private Boolean isExported = false;

    @Column(name = "sys_logdate")
    private LocalDateTime sysLogDate = LocalDateTime.now();

}