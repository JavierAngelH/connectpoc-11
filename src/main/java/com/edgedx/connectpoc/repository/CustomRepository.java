package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.dto.*;
import com.edgedx.connectpoc.model.ChartResults;
import com.edgedx.connectpoc.model.HealthStatisticsChartResults;

import java.util.List;

public interface CustomRepository {

    List<ChartResults> getGenexpertTestsByDate(String deviceName);

    List<ChartResults> getGenexpertTestsByDateLast7days(String deviceName);

    List<ChartResults> getGenexpertErrorsByDateLast7days(String deviceName);

    List<ChartResults> getGenexpertErrorsByDate(String deviceName);

    List<HealthStatisticsChartResults> getBatteryResultsByDateLast7days();

    List<HealthStatisticsChartResults> getNetworkSignalResultsByDateLast7days();

    List<ChartResults> getM2000TestsByDate(String deviceName);

    List<ChartResults> getM2000TestsByDateLast7days(String deviceName);

    List<ChartResults> getM2000ErrorsByDateLast7days(String deviceName);

    List<ChartResults> getM2000ErrorsByDate(String deviceName);

    List<GenexpertChaiDTO> getChaiHIVNotSentTests();

    List<AbbottChaiDTO> getChaiAbbottNotSentTests();

    List<EthiopiaCovidDTO> getEthiopiaGenexpertCovidNotSentData();

    List<EthiopiaCovidDTO> getEthiopiaAbbottCovidNotSentData();

    List<ChaiPatientSampleDTO> getChaiFacsPrestoNotSentTests();

    List<ChaiFacsprestoSummaryDTO>  getChaiSummaryDeviceDate(Integer month, Integer year);

}
