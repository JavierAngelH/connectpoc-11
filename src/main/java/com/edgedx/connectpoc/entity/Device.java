
package com.edgedx.connectpoc.entity;

import com.edgedx.connectpoc.model.DeviceType;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Device {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true)
	@NotEmpty(message = "This field is required")
	private String serialNumber;

	@Column(nullable = false)
	@NotNull(message = "This field is required")
	private DeviceType deviceType;

	private String testingPoint;

	@Column(nullable = false)
	private Boolean isExported = false;

}
