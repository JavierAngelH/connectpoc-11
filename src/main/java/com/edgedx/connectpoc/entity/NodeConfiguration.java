
package com.edgedx.connectpoc.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NodeConfiguration {

	@Id
	@Column(nullable = false, length = 200)
	@NotEmpty(message = "This field is required")
	@Size(max = 200)
	private String facility;

	@Column(nullable = false, length = 200)
	@NotEmpty(message = "This field is required")
	@Size(max = 200)
	private String country;

	@Column(nullable = false, length = 200)
	@NotEmpty(message = "This field is required")
	@Size(max = 200)
	private String province;

	@Column(nullable = false, length = 200)
	@Size(max = 200)
	@NotEmpty(message = "This field is required")
	private String district;

	@Column(nullable = false, length = 200)
	@NotEmpty(message = "This field is required")
	private String facilityCode;

	private Boolean isExported = false;

}
