package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "mgit", indexes = {
        @Index(name = "test_sequence_number", columnList = "test_sequence_number, accesion_number, start_date", unique = true)
})
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Mgit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Column(name = "version", length = 45)
    private String version;

    @Column(name = "datetime")
    private LocalDateTime datetime;

    @Column(name = "accesion_number", length = 45)
    private String accesionNumber;

    @Column(name = "isolate_number")
    private Integer isolateNumber;

    @Column(name = "test_id", length = 45)
    private String testId;

    @Column(name = "result_id_code", length = 45)
    private String resultIdCode;

    @Column(name = "test_sequence_number", length = 45)
    private String testSequenceNumber;

    @Column(name = "antibiotic", length = 4)
    private String antibiotic;

    @Column(name = "concentration", precision = 10)
    private Double concentration;

    @Column(name = "concentration_units", length = 45)
    private String concentrationUnits;

    @Column(name = "test_status", length = 45)
    private String testStatus;

    @Column(name = "growth_units")
    private Integer growthUnits;

    @Column(name = "ast_susceptibility", length = 45)
    private String astSusceptibility;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "result_date")
    private LocalDateTime resultDate;

    @Column(name = "instrument_type", length = 45)
    private String instrumentType;

    @Column(name = "protocol_length")
    private Integer protocolLength;

    @Column(name = "instrument_number")
    private Integer instrumentNumber;

    @Column(name = "instrument_location", length = 45)
    private String instrumentLocation;

    @Column(name = "preliminary_final_status", length = 1)
    private String preliminaryFinalStatus;

    @Column(name = "device_id", length = 45)
    private String deviceId;

    @Column(name = "is_exported")
    private Boolean isExported = false;

    @Column(name = "sys_logdate")
    private LocalDateTime sysLogDate = LocalDateTime.now();

}