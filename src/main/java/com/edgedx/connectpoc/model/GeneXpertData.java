package com.edgedx.connectpoc.model;

import com.edgedx.connectpoc.entity.GenexpertProbecheckDetail;
import com.edgedx.connectpoc.entity.GenexpertTestAnalyte;
import lombok.*;

import javax.persistence.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GeneXpertData {

	private String sampleId;
	private String patientId;
	private String testType;
	private String sampleType;
	private String status;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String errorStatus;
	private String reagentLotId;
	private LocalDate expirationDate;
	private String cartridgeSN;
	private String moduleName;
	private String moduleSN;
	private String instrumentSN;
	private String softwareVersion;
	private String testResult;
	private String testDisclaimer;
	private String meltPeaks;
	private String error;
	private String messages;
	private String history;
	private String notes;
	private String cartridgeIndex;
	private String detail;

	private List<GenexpertTestAnalyte> testAnalyteList;

	private List<GenexpertProbecheckDetail> probecheckDetailList;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GeneXpertData that = (GeneXpertData) o;
		return sampleId.equals(that.sampleId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sampleId);
	}
}
