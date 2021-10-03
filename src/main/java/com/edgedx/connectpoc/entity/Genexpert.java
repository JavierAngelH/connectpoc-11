package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Table(name = "genexpert", indexes = {
        @Index(name = "sample_id_UNIQUE", columnList = "sample_id, start_time, cartridge_sn, module_sn, end_time", unique = true)
})
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Genexpert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "assay", length = 45)
    private String assay;

    @Column(name = "assay_disclaimer", length = 200)
    private String assayDisclaimer;

    @Column(name = "assay_type", length = 45)
    private String assayType;

    @Column(name = "assay_version", length = 45)
    private String assayVersion;

    @Column(name = "device_name", length = 45)
    private String deviceName;

    @Column(name = "exported_date")
    private LocalDateTime exportedDate;

    @Column(name = "specific_parameters", length = 45)
    private String specificParameters;

    @Column(name = "reagent_lot_number", length = 45)
    private String reagentLotNumber;

    @Column(name = "user", length = 45)
    private String user;

    @Column(name = "cartridge_sn", length = 45)
    private String cartridgeSn;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "error", length = 600)
    private String error;

    @Column(name = "error_status", length = 45)
    private String errorStatus;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "history", length = 45)
    private String history;

    @Column(name = "instrument_sn", length = 45)
    private String instrumentSn;

    @Column(name = "melt_peaks", length = 45)
    private String meltPeaks;

    @Column(name = "messages", length = 45)
    private String messages;

    @Column(name = "module_name", length = 45)
    private String moduleName;

    @Column(name = "module_sn", length = 45)
    private String moduleSn;

    @Column(name = "notes", length = 200)
    private String notes;

    @Column(name = "patient_id", length = 45)
    private String patientId;

    @Column(name = "reagent_lot_id", length = 45)
    private String reagentLotId;

    @Column(name = "sample_id", length = 45)
    private String sampleId;

    @Column(name = "sample_type", length = 45)
    private String sampleType;

    @Column(name = "software_version", length = 45)
    private String softwareVersion;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "status", length = 45)
    private String status;

    @Column(name = "test_disclaimer", length = 200)
    private String testDisclaimer;

    @Column(name = "test_result", length = 200)
    private String testResult;

    @Column(name = "test_type", length = 200)
    private String testType;

    private Boolean isExported = false;

    @Column(name = "sys_logdate")
    private LocalDateTime sysLogDate = LocalDateTime.now();

    @Column(name = "cartridge_index", length = 45)
    private String cartridgeIndex;

    @Column(name = "detail", length = 45)
    private String detail;

    @Column(name = "api_exported")
    private Boolean apiExported  = false;

    private LocalDateTime nascopTimestamp;



}