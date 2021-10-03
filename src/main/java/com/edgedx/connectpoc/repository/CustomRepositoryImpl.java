package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.dto.*;
import com.edgedx.connectpoc.dto.mapper.AbbottChaiDTOMapper;
import com.edgedx.connectpoc.dto.mapper.ChaiFacsprestoSummaryDTOMapper;
import com.edgedx.connectpoc.dto.mapper.ChaiPatientSampleDTOMapper;
import com.edgedx.connectpoc.dto.mapper.GenexpertChaiDTOMapper;
import com.edgedx.connectpoc.model.ChartResults;
import com.edgedx.connectpoc.model.HealthStatisticsChartResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class CustomRepositoryImpl implements CustomRepository {

    @Autowired
    DataSource dataSource;

    private JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(this.dataSource);
    }

    @Override
    public List<ChartResults> getGenexpertTestsByDate(String deviceName) {
        return this.getJdbcTemplate().query("select count(*) as tests, date(start_time) as date from " +
                "genexpert where device_name = ? group by date", new String[]{deviceName}, BeanPropertyRowMapper.newInstance(ChartResults.class));
    }

    @Override
    public List<ChartResults> getGenexpertTestsByDateLast7days(String deviceName) {
        return this.getJdbcTemplate().query("select count(*) as tests, date(start_time) as date from " +
                "genexpert where device_name = ? group by date order by date desc limit 7", new String[]{deviceName}, BeanPropertyRowMapper.newInstance(ChartResults.class));
    }

    @Override
    public List<ChartResults> getGenexpertErrorsByDateLast7days(String deviceName) {
        return this.getJdbcTemplate().query("select count(*) as tests, date(start_time) as date from " +
                "genexpert where device_name = ? and error is not null and error not in ('', '<None>', 'NULL') group by date order by date desc limit 7", new String[]{deviceName}, BeanPropertyRowMapper.newInstance(ChartResults.class));
    }

    @Override
    public List<ChartResults> getGenexpertErrorsByDate(String deviceName) {
        return this.getJdbcTemplate().query("select count(*) as tests, date(start_time) as date from " +
                "genexpert where device_name = ? and error is not null and error not in ('', '<None>', 'NULL') group by date", new String[]{deviceName}, BeanPropertyRowMapper.newInstance(ChartResults.class));
    }

    @Override
    public List<HealthStatisticsChartResults> getBatteryResultsByDateLast7days() {
        return this.getJdbcTemplate().query("select battery_status as 'value', log_date as time from " +
                "node_health_statistics order by time asc", BeanPropertyRowMapper.newInstance(HealthStatisticsChartResults.class));
    }

    @Override
    public List<HealthStatisticsChartResults> getNetworkSignalResultsByDateLast7days() {
        return this.getJdbcTemplate().query("select network_signal_strength as 'value', log_date as time from " +
                "node_health_statistics order by time asc", BeanPropertyRowMapper.newInstance(HealthStatisticsChartResults.class));
    }

    @Override
    public List<ChartResults> getM2000TestsByDate(String deviceName) {
        return this.getJdbcTemplate().query("select count(*) as tests, date(test_datetime) as date from " +
                "abbott_m2000 where device_code = ? group by date", new String[]{deviceName}, BeanPropertyRowMapper.newInstance(ChartResults.class));
    }

    @Override
    public List<ChartResults> getM2000TestsByDateLast7days(String deviceName) {
        return this.getJdbcTemplate().query("select count(*) as tests, date(test_datetime) as date from " +
                "abbott_m2000 where device_code = ? group by date order by date desc limit 7", new String[]{deviceName}, BeanPropertyRowMapper.newInstance(ChartResults.class));
    }

    @Override
    public List<ChartResults> getM2000ErrorsByDateLast7days(String deviceName) {
        return this.getJdbcTemplate().query("select count(*) as tests, date(test_datetime) as date from " +
                "abbott_m2000 where device_code = ? and test_error is not null and test_error not in ('') group by date order by date desc limit 7", new String[]{deviceName}, BeanPropertyRowMapper.newInstance(ChartResults.class));
    }

    @Override
    public List<ChartResults> getM2000ErrorsByDate(String deviceName) {
        return this.getJdbcTemplate().query("select count(*) as tests, date(test_datetime) as date from " +
                "abbott_m2000 where device_code = ? and test_error is not null and test_error not in ('') group by date", new String[]{deviceName}, BeanPropertyRowMapper.newInstance(ChartResults.class));
    }

    @Override
    public List<GenexpertChaiDTO> getChaiHIVNotSentTests() {
        return this.getJdbcTemplate().query(
                "SELECT genexpert.*, node_configuration.facility from genexpert, nodeconfiguration where genexpert.api_exported = 0 and UPPER(genexpert.assay) like '%HIV%' and sample_id not like 'TEST_%'",
                new GenexpertChaiDTOMapper());
    }

    @Override
    public List<AbbottChaiDTO> getChaiAbbottNotSentTests() {
        return this.getJdbcTemplate().query(
                "SELECT abbott_m2000.*, nodeconfiguration.facility from abbott_m2000, nodeconfiguration where abbott_m2000.api_exported = 0 or api_response = 503",
                new AbbottChaiDTOMapper());
    }

    @Override
    public List<EthiopiaCovidDTO> getEthiopiaGenexpertCovidNotSentData() {
        return this.getJdbcTemplate().query(
                "SELECT genexpert.id, genexpert.sample_id, genexpert.test_result as result from genexpert where assay = 'Xpert Xpress SARS-CoV-2' and genexpert.api_exported = 0",
                BeanPropertyRowMapper.newInstance(EthiopiaCovidDTO.class));
    }

    @Override
    public List<EthiopiaCovidDTO> getEthiopiaAbbottCovidNotSentData() {
        return this.getJdbcTemplate().query(
                "SELECT abbott_m2000.id, abbott_m2000.sample_id, abbott_m2000.result from abbott_m2000 where assay_name = 'SARS-COV-2' and (abbott_m2000.api_exported = 0 or api_response = 503)",
                BeanPropertyRowMapper.newInstance(EthiopiaCovidDTO.class));
    }

    @Override
    public List<ChaiPatientSampleDTO> getChaiFacsPrestoNotSentTests() {
        return  this.getJdbcTemplate().query("SELECT * from facsprestopatientsample where api_exported = 0", new ChaiPatientSampleDTOMapper());
    }

    @Override
    public List<ChaiFacsprestoSummaryDTO> getChaiSummaryDeviceDate(Integer month, Integer year) {
        String query = "SELECT T2.device_code, T2.lab_name, T2.month, T2.year, T2.tests, IFNULL(T1.used_cartridges, 0) as used_cartridges FROM "
                + "(SELECT h.serial_number, h.month, h.year, h.used_cartridges "
                + "from device_cartridge_historic h where month = ? AND year = ?) T1 RIGHT JOIN "
                + "(SELECT sum(total) tests, lab_name, device_code, month, year from ("
                + "(SELECT count(*) total, device_code, lab_name,MONTH(run_datetime) as month, YEAR(run_datetime) as year from facspresto_patient_sample  where MONTH(run_datetime) = ?"
                + "AND YEAR(run_datetime) = ?  group by month, year)) AS total_tables group by month, year, device_code) T2 "
                + "ON T1.serial_number = T2.device_code AND T1.month = T2.month AND T1.year = T2.year ";
        Object[] args = { month, year, month, year, month, year, month, year, month, year };
        return this.getJdbcTemplate().query(query, args, new ChaiFacsprestoSummaryDTOMapper());
    }

}
