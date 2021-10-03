package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.*;

@Table(name = "genexpert_test_analyte", indexes = {
        @Index(name = "UNIQUE", columnList = "analyte_name, test_id", unique = true)
})
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class GenexpertTestAnalyte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "test_id")
    private Long testId;

    @Column(name = "analyte_name", length = 45)
    private String analyteName;

    @Column(name = "ct", length = 45)
    private String ct;

    @Column(name = "end_pt", length = 45)
    private String endPt;

    @Column(name = "analyte_result", length = 45)
    private String analyteResult;

    @Column(name = "probe_check_result", length = 45)
    private String probeCheckResult;

    private Boolean isExported = false;

    @Column(name = "device_name", length = 45)
    private String deviceName;


}