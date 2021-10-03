package com.edgedx.connectpoc.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GeneXpertGeneralData {
	private String deviceName;
	private LocalDateTime exportedDate;
	private String user;
	private String assay;
	private String assayVersion;
	private String assayType;
	private String specificParameters;
	private String reagentLotNumber;
	private String assayDisclaimer;
	
}
