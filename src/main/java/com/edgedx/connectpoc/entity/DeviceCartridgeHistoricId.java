package com.edgedx.connectpoc.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DeviceCartridgeHistoricId implements Serializable {
    @Column(name = "serial_number", nullable = false, length = 50)
    private String serialNumber;
    @Column(name = "month", nullable = false)
    private Integer month;
    @Column(name = "year", nullable = false)
    private Integer year;

}