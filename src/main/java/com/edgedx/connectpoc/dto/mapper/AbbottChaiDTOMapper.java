package com.edgedx.connectpoc.dto.mapper;

import com.edgedx.connectpoc.dto.AbbottChaiDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AbbottChaiDTOMapper implements RowMapper<AbbottChaiDTO> {

    @Override
    public AbbottChaiDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		AbbottChaiDTO object = new AbbottChaiDTO();
    	object.setId(rs.getLong("id"));
	object.setAssayName(rs.getString("assay_name"));
	String carrierId = rs.getString("carrier_id");
	object.setCarrierId(carrierId.replace("`", ""));
	object.setResults(rs.getString("result"));
	object.setSampleId(rs.getString("sample_id"));
	object.setTestdatetime(rs.getTimestamp("test_datetime"));
	object.setTestError(rs.getString("test_error"));
	object.setUnits(rs.getString("units"));
	return object;
    }

}
