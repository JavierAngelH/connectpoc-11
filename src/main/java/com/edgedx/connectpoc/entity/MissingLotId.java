package com.edgedx.connectpoc.entity;

import com.edgedx.connectpoc.model.DeviceType;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MissingLotId {

    @Id
    @Column(name = "lot_id_number", nullable = false, length = 50)
    private String lotIdNumber;

    private DeviceType deviceType;

}