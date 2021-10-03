package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DeviceCartridgeHistoric {

    @EmbeddedId
    private DeviceCartridgeHistoricId id;

    @Column(name = "used_cartridges")
    private Integer usedCartridges;

}