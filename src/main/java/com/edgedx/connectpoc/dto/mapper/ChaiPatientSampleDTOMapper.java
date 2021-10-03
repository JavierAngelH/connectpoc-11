package com.edgedx.connectpoc.dto.mapper;

import com.edgedx.connectpoc.dto.ChaiPatientSampleDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class ChaiPatientSampleDTOMapper  implements RowMapper<ChaiPatientSampleDTO> {

    SimpleDateFormat rdtFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");

    SimpleDateFormat rleFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public ChaiPatientSampleDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ChaiPatientSampleDTO object = new ChaiPatientSampleDTO();
        object.setId(rs.getLong("id"));
        object.setInstrumentserialno(rs.getString("device_code"));
        object.setLaboratory(rs.getString("lab_name"));
        object.setRunId(rs.getString("run_id"));
        object.setRunDatetime(rdtFormat.format(rs.getDate("run_datetime")));
        object.setOperator(rs.getString("operator"));
        object.setReagentLotId(rs.getString("reagent_lot_id"));
        object.setReagentLotExp(rleFormat.format(rs.getDate("ReagentLotExpDate")));
        object.setProcessLotId("");
        object.setProcessLotExp("");
        object.setPatientId(rs.getString("patient_id"));
        object.setInstQcPassed(rs.getString("inst_qc_passed"));
        object.setReagentQcPassed(rs.getString("reagent_qc_passed"));
        object.setCd4(rs.getString("cd4"));
        object.setPercentCd4(String.valueOf(rs.getDouble("percent_cd4")));
        object.setHb(String.valueOf(rs.getDouble("hb")));
        object.setErrorCode(rs.getString("error_code"));
        return object;
    }


}
