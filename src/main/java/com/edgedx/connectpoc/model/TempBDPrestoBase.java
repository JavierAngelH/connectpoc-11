package com.edgedx.connectpoc.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TempBDPrestoBase {

	private Integer id;
	private String version;
	private String deviceCode;
	private String labName;
	private Integer instrumentQcStartId;
	private Integer cd4ProcessStartId;
	private Integer hbProcessStartId;
	private Integer patientSampleStartId;
	private List<TempBDPresto> tempBDPrestoList;
	private String errorMsg;
	private Boolean validInstrumentQc = false;
	private Boolean validCD4Process = false;
	private Boolean validHbProcess = false;
	private Boolean validPatientSample = false;
	private Boolean isValid = false;
	private Boolean validPimaCd4 = false;
	private Boolean validPimaBeads = false;

}
