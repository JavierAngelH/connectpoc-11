package com.edgedx.connectpoc.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MgitGeneralData  {
	
	private String senderName;
	private String version;
	private LocalDateTime datetime;
	private String accessionNumber;
	private Integer isolateNumber;
	private String testId;
	private Set<MgitData> resultList;
	private String deviceId;

}
