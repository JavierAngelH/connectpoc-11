package com.edgedx.connectpoc.entity;

import lombok.*;
import org.apache.tomcat.jni.Local;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "abbott_m2000", indexes = {
        @Index(name = "device_code", columnList = "device_code, sample_id, test_datetime, sample_position", unique = true)
})
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AbbottM2000 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "device_code", nullable = false, length = 45)
    private String deviceCode;

    @Column(name = "sender_name", length = 45)
    private String senderName;

    @Column(name = "version", length = 45)
    private String version;

    @Column(name = "export_datetime")
    private LocalDateTime exportDatetime;

    @Column(name = "sample_id", nullable = false, length = 45)
    private String sampleId;

    @Column(name = "carrier_id", length = 45)
    private String carrierId;

    @Column(name = "sample_position", length = 45)
    private String samplePosition;

    @Column(name = "assay_number", length = 45)
    private String assayNumber;

    @Column(name = "assay_name", length = 45)
    private String assayName;

    @Column(name = "report_type", length = 45)
    private String reportType;

    @Column(name = "reagent_lot_id", length = 45)
    private String reagentLotId;

    @Column(name = "reagent_serial_number", length = 45)
    private String reagentSerialNumber;

    @Column(name = "control_lot_number", length = 45)
    private String controlLotNumber;

    @Column(name = "result", length = 45)
    private String result;

    @Column(name = "units", length = 45)
    private String units;

    @Column(name = "reference_range", length = 45)
    private String referenceRange;

    @Column(name = "operator", length = 45)
    private String operator;

    @Column(name = "test_error", length = 200)
    private String testError;

    @Column(name = "test_datetime", nullable = false)
    private LocalDateTime testDatetime;

    private Boolean isExported = false;

    @Column(name = "sys_logdate")
    private LocalDateTime sysLogdate = LocalDateTime.now();

    @Column(name = "api_exported")
    private Boolean apiExported = false;

    @Column(name = "nascop_timestamp")
    private LocalDateTime nascopTimestamp;

    @Column(name = "api_response")
    private Integer apiResponse;


}