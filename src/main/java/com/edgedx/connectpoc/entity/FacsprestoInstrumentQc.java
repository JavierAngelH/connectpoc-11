package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
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
public class FacsprestoInstrumentQc {
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

    private Integer normalCount;

    private Integer lowCount;

    @Column(length = 50)
    private String pass;

    @Column(length = 250)
    private String errorCode;

    private Boolean isExported = false;

    private LocalDateTime sysLogdate = LocalDateTime.now();


}