package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Table(name = "triage", indexes = {
        @Index(name = "result_datetime", columnList = "result_datetime, patient_result_serial, patient_id, instrument_serial", unique = true)
})
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Triage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "sender_id", length = 100)
    private String senderId;

    @Column(name = "datetime")
    private LocalDateTime datetime;

    @Column(name = "version", length = 45)
    private String version;

    @Column(name = "process_id", length = 1)
    private String processId;

    @Column(name = "patient_id", length = 45)
    private String patientId;

    @Column(name = "lab_patient_id", length = 45)
    private String labPatientId;

    @Column(name = "instrument_serial", length = 8)
    private String instrumentSerial;

    @Column(name = "patient_result_serial", length = 5)
    private String patientResultSerial;

    @Column(name = "panel_type", length = 45)
    private String panelType;

    @Column(name = "reagent_lot_number", length = 45)
    private String reagentLotNumber;

    @Column(name = "priority", length = 45)
    private String priority;

    @Column(name = "qc_result_code", length = 8)
    private String qcResultCode;

    @Column(name = "patient_result_approval", length = 45)
    private String patientResultApproval;

    @Column(name = "result_datetime")
    private LocalDateTime resultDatetime;

    @Column(name = "report_type", length = 1)
    private String reportType;

    @Column(name = "test_id", length = 45)
    private String testId;

    @Column(name = "data_value", length = 45)
    private String dataValue;

    @Column(name = "units", length = 45)
    private String units;

    @Column(name = "`range`", length = 45)
    private String range;

    @Column(name = "normalcy_flag", length = 45)
    private String normalcyFlag;

    @Column(name = "population", length = 1)
    private String population;

    @Column(name = "result_status", length = 1)
    private String resultStatus;

    @Column(name = "operator_id", length = 45)
    private String operatorId;

    @Column(name = "device_id", length = 45)
    private String deviceId;

    @Column(name = "is_exported")
    private Boolean isExported = false;

    @Column(name = "sys_logdate")
    private LocalDateTime sysLogDate = LocalDateTime.now();

}