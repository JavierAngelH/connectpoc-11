package com.edgedx.connectpoc.dto.mapper;

import com.edgedx.connectpoc.dto.ChaiFacsprestoSummaryDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChaiFacsprestoSummaryDTOMapper implements RowMapper<ChaiFacsprestoSummaryDTO> {

    @Override
    public ChaiFacsprestoSummaryDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ChaiFacsprestoSummaryDTO object = new ChaiFacsprestoSummaryDTO();
        object.setInstrumentSerialNo(rs.getString("device_code"));
        object.setLaboratory(rs.getString("lab_name"));
        object.setMonth(rs.getInt("month"));
        object.setYear(rs.getInt("year"));
        object.setTotalTestsDone(rs.getInt("tests"));
        object.setCartridgeUsed(rs.getInt("used_cartridges"));
        return object;
    }


}
