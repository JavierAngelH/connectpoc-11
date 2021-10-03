package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.*;

@Table(name = "genexpert_probecheck_detail", indexes = {
        @Index(name = "UNIQUE", columnList = "test_id, analyte_name", unique = true)
})
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class GenexpertProbecheckDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "test_id")
    private Long testId;

    @Column(name = "analyte_name", length = 45)
    private String analyteName;

    @Column(name = "prb_chk_1", length = 45)
    private String prbChk1;

    @Column(name = "prb_chk_2", length = 45)
    private String prbChk2;

    @Column(name = "prb_chk_3", length = 45)
    private String prbChk3;

    @Column(name = "probe_check_result", length = 45)
    private String probeCheckResult;

    private Boolean isExported = false;

    @Column(name = "device_name", length = 45)
    private String deviceName;

}