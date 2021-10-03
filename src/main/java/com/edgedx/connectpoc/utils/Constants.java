package com.edgedx.connectpoc.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Constants {

    public static final String AES_MYSQL_KEY = "ConnectPOC2017*";

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    public static final String[] instrumentQcResultsTemplateFormat = { "Run ID", "Run Date/Time", "Operator", "Normal count", "Low count", "Passed?",
            "Error Codes" };

    public static final String[] cD4ProcessCtrlResultsTemplateFormat = { "Run ID", "Run Date/Time", "Operator", "Reagent Lot ID", "Reagent Lot Exp",
            "Process Lot ID", "Process Lot Exp", "Level", "Exp CD4 (Lwr)", "Exp CD4 (Upr)", "Exp %CD4 (Lwr)", "Exp %CD4 (Upr)", "Reagent QC P/F", "CD4", "%CD4",
            "Passed?", "Error Codes" };

    public static final String[] hbProcessCtrlResultsTemplateFormat = { "Run ID", "Run Date/Time", "Operator", "Reagent Lot ID", "Reagent Lot Exp",
            "Process Lot ID", "Process Lot Exp", "Level", "Exp Hb (Lwr)", "Exp Hb (Upr)", "Reagent QC P/F", "Hb (g/dL)", "Passed?", "Error Codes" };

    public static final String[] patientSampleResultsTemplateFormat = { "Run ID", "Run Date/Time", "Operator", "Reagent Lot ID", "Reagent Lot Exp",
            "Patient ID", "Inst QC Passed?", "Reagent QC Passed?", "CD4", "%CD4", "Hb", "Error Codes" };

    public static final String[] instrumentQcResultsTemplateFormat_H = { "Mac ID", "Testing Point", "Country", "Province", "District", "Facility",
            "SerialNumber", "Run ID", "Run Date/Time", "Operator", "Normal count", "Low count", "Passed?", "Error Codes" };

    public static final String[] cD4ProcessCtrlResultsTemplateFormat_H = { "Mac ID", "Testing Point", "Country", "Province", "District", "Facility",
            "SerialNumber", "Run ID", "Run Date/Time", "Operator", "Reagent Lot ID", "Reagent Lot Exp", "Process Lot ID", "Process Lot Exp", "Level",
            "Exp CD4 (Lwr)", "Exp CD4 (Upr)", "Exp %CD4 (Lwr)", "Exp %CD4 (Upr)", "Reagent QC P/F", "CD4", "%CD4", "Passed?", "Error Codes" };

    public static final String[] hbProcessCtrlResultsTemplateFormat_H = { "Mac ID", "Testing Point", "Country", "Province", "District", "Facility",
            "SerialNumber", "Run ID", "Run Date/Time", "Operator", "Reagent Lot ID", "Reagent Lot Exp", "Process Lot ID", "Process Lot Exp", "Level",
            "Exp Hb (Lwr)", "Exp Hb (Upr)", "Reagent QC P/F", "Hb (g/dL)", "Passed?", "Error Codes" };

    public static final String[] patientSampleResultsTemplateFormat_H = { "Mac ID", "Testing Point", "Country", "Province", "District", "Facility",
            "SerialNumber", "Run ID", "Run Date/Time", "Operator", "Reagent Lot ID", "Reagent Lot Exp", "Patient ID", "Inst QC Passed?", "Reagent QC Passed?",
            "CD4", "%CD4", "Hb", "Error Codes", "Imported by node on", "Sent to nascop on" };

    public static final String[] nodeStatTemplateFormat_H = { "Country", "Province", "District", "Facility", "Mac ID", "Lat", "Long", "Battery Status",
            "Network Operator", "Network Signal", "Temperature", "Humidity", "Ambient Light", "Last Log Date", "Node ID", "Power" };

    public static final String[] nodeConfigTemplateFormat_H = { "Country", "Province", "District", "Facility", "Facility ID", "Mac ID" };

    public static final String[] nodeDevicesTemplateFormat_H = { "Serial Number", "Testing Point", "Type Name", "Mac ID" };

    public static final List<String> pimaCd4TemplateFormat = Arrays.asList("Test ID", "Device ID", "Assay ID", "Assay Name", "Sample",
            "CD3+CD4+ Value [cells/mm3]", "ErrorMessage", "Operator", "Result Date", "Start Time", "Barcode", "Expiry Date", "Volume", "Device", "Reagent",
            "Software Version");

    public static final List<String> pimaBeadsTemplateFormat = Arrays.asList("Test ID", "Device ID", "Assay ID", "Assay Name", "Sample",
            "CD3+CD4+ Value [cells/mm3]", "ErrorMessage", "Operator", "Result Date", "Start Time", "Barcode", "Expiry Date", "Device", "Software Version");

    public static final String[] pimaBeadsTemplateFormat_H = { "Mac ID", "Testing Point", "Country", "Province", "District", "Facility", "SerialNumber",
            "Test ID", "Device ID", "Assay ID", "Assay Name", "Sample", "CD3+CD4+ Value [cells/mm3]", "ErrorMessage", "Operator", "Result Date", "Start Time",
            "Barcode", "Expiry Date", "Device", "Software Version" };

    public static final String[] pimaCd4TemplateFormat_H = { "Mac ID", "Testing Point", "Country", "Province", "District", "Facility", "SerialNumber",
            "Test ID", "Device ID", "Assay ID", "Assay Name", "Sample", "CD3+CD4+ Value [cells/mm3]", "ErrorMessage", "Operator", "Result Date", "Start Time",
            "Barcode", "Expiry Date", "Volume", "Device", "Reagent", "Software Version" };

    public static final String[] geneXpertTemplateFormat_H = { "Mac ID", "Testing Point", "Country", "Province", "District", "Facility", "SerialNumber",
            "Assay", "Assay Disclaimer", "Assay Type", "Assay Version", "Exported Date", "Need Lot Specific Parameters", "Reagent Lot Number", "User",
            "Cartridge S/N", "End Time", "Error", "Error Status", "Expiration Date", "History", "Instrument S/N", "Melt Peaks", "Messages", "Module Name",
            "Module S/N", "Notes", "Patiend ID", "Reagent Lot ID", "Sample ID", "Sample Type", "Software Version", "Start Time", "Status", "Test Disclaimer",
            "Test Result", "Test Type", "Result ID", "Imported by node on", "Sent to nascop on" };

    public static final String[] geneXpertProbeCheckTemplateFormat_H = { "Test ID", "Analyte Name", "PRB CHK 1", "PRB CHK 2", "PRB CHK 3", "Probe Check Result",
            "Device Name" };

    public static final String[] geneXpertTestAnalyteTemplateFormat_H = { "Test ID", "Analyte Name", "CT", "End PT", "Analyte Result", "Probe Check Result",
            "Device Name" };

    public static final List<String> nodeStatTemplateFormat = Arrays.asList("mac_id", "timestamp", "network_operator", "device_latitude", "device_longitude",
            "cell_signal_strength", "battery_level", "temperature", "humidity", "ambient_light");

    public static final List<String> incompleteNodeStatTemplateFormat = Arrays.asList("mac_id", "timestamp", "network_operator", "device_latitude",
            "device_longitude", "cell_signal_strength", "battery_level");

    public static final String[] mgitTemplateFormat_H = { "Mac ID", "Testing Point", "Country", "Province", "District", "Facility", "SerialNumber",
            "Sender Name", "Version", "Datetime", "Accession Number", "Isolate Number", "Test id", "Result id code", "Test Sequence Number", "Antibiotic",
            "Concentration", "Concentration Units", "Test Status", "Growth Units", "AST Susceptibility", "Start Date", "Result Date", "Instrument Type",
            "Protocol Length", "Instrument Number", "Instrument Location", "Preliminary/Final Status" };

    public static final List<String> reportedProblemsTemplateFormat = Arrays.asList("Mac ID", "Testing Point", "Country", "Province", "District", "Facility",
            "Problem Description", "Problem Type ID", "Problem Type Description", "Error Code", "Device Type", "Serial Number", "Date");

    public static final String[] triageTemplateFormat_H = { "Mac ID", "Testing Point", "Country", "Province", "District", "Facility", "SerialNumber",
            "Sender Name", "Version", "Datetime", "Process ID", "Patient ID", "Lab Patient ID", "Instrument Serial", "Patient Result Serial", "Panel Type",
            "Reagent Lot Number", "Priority", "QC Result Code", "Patient Result Approval", "Result Datetime", "Report Type", "Test ID", "Data Value",
            "Units", "Range", "Normalcy Flag", "Population", "Result Status", "Operator ID" };

    public static final String[] abbottTemplateFormat_H = { "Mac ID", "Testing Point", "Country", "Province", "District", "Facility", "SerialNumber",
            "Sender Name", "Version", "Export Date", "Sample ID", "Carrier ID", "Sample Position", "Assay Number", "Assay Name",
            "Report Type", "Reagent Lot ID", "Reagent Serial Number", "Control Lot Number", "Result", "Units", "Reference Range", "Operator", "Test Error",
            "Test Datetime", "Imported by node on", "Sent to nascop on" };

    public static enum PIMA_CATEGORY {
        PIMA_CD4,
        PIMA_BEADS;
    }

    public static enum PRESTO_CATEGORY {
        CD4_CONTROL_PROCESS,
        HB_CONTROL_PROCESS,
        INSTRUMENT_QC,
        PATIENT_SAMPLE;
    }

    public static final String FTP_APP_URL = "http://localhost:8080/ConnectPOC/ftp?";

    public static final String START_OF_TEXT = "\2";

    public static final String END_OF_TEXT = "\3";


    public static final String MGIT_END = "L,1,N";
}
