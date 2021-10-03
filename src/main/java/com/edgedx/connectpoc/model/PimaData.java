package com.edgedx.connectpoc.model;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PimaData {

	private Integer id;
	private Integer testId;
	private String deviceId;
	private String assayId;
	private String sample;
	private Integer cd3cd4;
	private String errorMessage;
	private String operator;
	private LocalDate resultDate;
	private LocalDateTime startTime;
	private String barcode;
	private String expiryDate;
	private String device;
	private String softwareVersion;
	private String volume;
	private String reagent;
	private String assayName;

}
