package com.edgedx.connectpoc.dto.mapper;

import com.edgedx.connectpoc.dto.GenexpertChaiDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GenexpertChaiDTOMapper implements RowMapper<GenexpertChaiDTO> {

    SimpleDateFormat rleFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public GenexpertChaiDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        GenexpertChaiDTO object = new GenexpertChaiDTO();
        String rawResultId = rs.getString("test_result");
        String resultId = "";
        String resultIdInterpretation = "";
        if (!rawResultId.contains("HIV-1 DETECTED"))
            resultId = rawResultId.replaceAll("\\|", "").trim();
        else {
            resultId = "HIV-1 DETECTED";
            String[] resultArray = rawResultId.replace("HIV-1 DETECTED", "").replaceAll("\\|", "").trim().split(" ");
            if (resultArray.length > 1) {
                if (rawResultId.contains("<") || rawResultId.contains(">")) {
                    resultIdInterpretation = resultArray[0] + " " + resultArray[1];
                } else {
                    resultIdInterpretation = resultArray[0];
                }
            }
            if (resultIdInterpretation.contains("E")) {
                try {
                    Float resultIdFloat = Float.valueOf(resultIdInterpretation);
                    resultIdInterpretation = String.format("%.0f", resultIdFloat);
                } catch (Exception e) {
                    resultIdInterpretation = resultArray[0] + " " + resultArray[1];
                }
            }
        }
        try {
            object.setExportedDate(rleFormat.format(rs.getDate("exported_date")));
        } catch (Exception e) {
            object.setExportedDate(rleFormat.format(new Date()));
        }
        object.setAssay(rs.getString("assay"));
        object.setAssayVersion(rs.getString("assay_version"));
        object.setSampleId(rs.getString("sample_id"));
        object.setPatientId(rs.getString("patient_id"));
        object.setUser(rs.getString("user"));
        String status = rs.getString("status");
        if (status.equals("$STATUS"))
            status = "error";
        object.setStatus(status);
        if (rs.getDate("start_time") != null)
            object.setStartTime(rleFormat.format(rs.getDate("start_time")));
        if (rs.getDate("end_time") != null)
            object.setEndTime(rleFormat.format(rs.getDate("end_time")));
        String errorStatus = rs.getString("error_status");
        if (errorStatus.equals("$ESTATUS"))
            status = "error";
        object.setErrorStatus(errorStatus);
        object.setReagentLotId(rs.getString("reagent_lot_id"));
        object.setCartridgeExpirationDate(rs.getString("expiration_date"));
        object.setCartridgeSerial(rs.getString("cartridge_sn"));
        object.setModuleName(rs.getString("module_name"));
        object.setModuleSerial(rs.getString("module_sn"));
        object.setInstrumentSerial(rs.getString("instrument_sn"));
        object.setSoftwareVersion(rs.getString("software_version"));
        object.setResultId(resultId);
        object.setResultIdinterpretation(resultIdInterpretation);
        object.setDeviceSerial(rs.getString("module_sn"));
        object.setSystemName(rs.getString("facility"));
        object.setPassword("80SHe5MF7gM73t4aog5Q");
        object.setId(rs.getLong("id"));
        return object;
    }


}
