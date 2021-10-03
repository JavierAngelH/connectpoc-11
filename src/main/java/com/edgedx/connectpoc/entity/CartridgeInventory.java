/**
 * CartridgeInventory.java Created: Jul 27, 2017 JavierAngelH
 */

package com.edgedx.connectpoc.entity;

import com.edgedx.connectpoc.model.CartridgeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CartridgeInventory {

    @Id
    @NotNull(message = "This field is required")
    @Size(max = 50)
    private String lotId;

    private LocalDate expirationDate;

    @NotNull(message = "This field is required")
    private Integer quantity;

    @NotNull(message = "This field is required")
    private CartridgeType cartridgeType;

    @ManyToOne
    @NotNull(message = "This field is required")
    @JsonIgnore
    private Device device;

    @Transient
    private String deviceSerialNumber;

}
