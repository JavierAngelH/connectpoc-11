package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.entity.*;
import com.edgedx.connectpoc.model.DeviceType;
import com.edgedx.connectpoc.model.NotificationMessage;
import com.edgedx.connectpoc.model.TempBDPresto;
import com.edgedx.connectpoc.model.TempBDPrestoBase;
import com.edgedx.connectpoc.repository.*;
import com.edgedx.connectpoc.utils.Constants;
import com.edgedx.connectpoc.utils.DateUtils;
import com.edgedx.connectpoc.views.Broadcaster;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.Cleanup;
import lombok.extern.java.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

@Service
@Log
public class FacsprestoServiceImpl implements FacsprestoService {

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    NodeHealthStatisticsRepository nodeHealthStatisticsRepository;

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Autowired
    MessageNotificationRepository messageNotificationRepository;

    @Autowired
    CartridgeInventoryRepository cartridgeInventoryRepository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    FacsprestoCd4ControlRepository facsprestoCd4ControlRepository;

    @Autowired
    FacsprestoHbControlRepository facsprestoHbControlRepository;

    @Autowired
    FacsprestoInstrumentQcRepository facsprestoInstrumentQcRepository;

    @Autowired
    FacsprestoPatientSampleRepository facsprestoPatientSampleRepository;

    @Autowired
    CartridgeInventoryAPIService cartridgeInventoryAPIService;

    @Autowired
    PatientSampleFacsprestoAPIService patientSampleFacsprestoAPIService;

    @Override
    public void importFacsprestoFiles(File sourceFolder) {
        try {
            // get downloaded files with .csv extension
            File[] files = sourceFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".csv");
                }
            });
            // if files are found; loop through files
            if (files != null) {
                for (File child : files) {
                    // try Parsing file as BD presto File
                    List<TempBDPresto> tempPrestos = parsePrestoFile(child);
                    if (tempPrestos != null) {
                        TempBDPrestoBase tempPrestoBase = isPrestoFileValid(tempPrestos);
                        if (tempPrestoBase.getIsValid()) {
                            // save base file record
                            importPrestoToDatabase(tempPrestos, tempPrestoBase);
                            log.info(child.getName() + "== file imported");
                            String targetDirectory = propertiesService.getFullPropertyValue("facspresto.backup.folder");
                            FileUtils.moveFileToDirectory(child, new File(targetDirectory), false);
                        }
                    } else {
                        log.log(Level.SEVERE, child.getName() + " not valid");
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private List<TempBDPresto> parsePrestoFile(File sourceFile) throws IOException {
        List<TempBDPresto> tempPrestos = new ArrayList<>();
        // Create the CSVFormat object
        CSVFormat format = CSVFormat.EXCEL;// .withDelimiter(',');
        // initialize the CSVParser object
        @Cleanup CSVParser parser = new CSVParser(new FileReader(sourceFile), format);
        int pid = 0;
        for (CSVRecord record : parser) {
            TempBDPresto tempPresto = new TempBDPresto();
            tempPresto.setId(pid);
            for (int i = 0; i < record.size(); i++) {
                tempPresto = this.setPrestoValue(tempPresto, i, record.get(i));
            }
            tempPrestos.add(tempPresto);
            pid++;
        }
        return tempPrestos;
    }

    private TempBDPrestoBase isPrestoFileValid(List<TempBDPresto> tempPrestos) {
        TempBDPrestoBase tempPrestoBase = new TempBDPrestoBase();
        try {
            if (tempPrestos.size() > 4) {
                tempPrestoBase.setVersion(tempPrestos.get(1).getC0());
                tempPrestoBase.setDeviceCode(tempPrestos.get(3).getC1());
                tempPrestoBase.setLabName(tempPrestos.get(4).getC1());
                tempPrestoBase = this.checkInstrumentQcResults(tempPrestoBase, tempPrestos);
                tempPrestoBase = this.checkCD4ProcessCtrlResults(tempPrestoBase, tempPrestos);
                tempPrestoBase = this.checkHbProcessCtrlResults(tempPrestoBase, tempPrestos);
                tempPrestoBase = this.checkPatientSampleResults(tempPrestoBase, tempPrestos);

                if (tempPrestoBase.getValidCD4Process() || tempPrestoBase.getValidInstrumentQc()
                        || tempPrestoBase.getValidHbProcess() || tempPrestoBase.getValidPatientSample()) {
                    tempPrestoBase.setIsValid(true);
                    tempPrestoBase.setTempBDPrestoList(tempPrestos);
                }
            }
            return tempPrestoBase;

        } catch (Exception e) {
            log.log(Level.SEVERE, "file not valid Presto file", e);
            return tempPrestoBase;
        }
    }

    private TempBDPresto setPrestoValue(TempBDPresto tempbdpresto, int c, String value) {
        try {
            switch (c) {
                case 0:
                    tempbdpresto.setC0(value);
                    return tempbdpresto;
                case 1:
                    tempbdpresto.setC1(value);
                    return tempbdpresto;
                case 2:
                    tempbdpresto.setC2(value);
                    return tempbdpresto;
                case 3:
                    tempbdpresto.setC3(value);
                    return tempbdpresto;
                case 4:
                    tempbdpresto.setC4(value);
                    return tempbdpresto;
                case 5:
                    tempbdpresto.setC5(value);
                    return tempbdpresto;
                case 6:
                    tempbdpresto.setC6(value);
                    return tempbdpresto;
                case 7:
                    tempbdpresto.setC7(value);
                    return tempbdpresto;
                case 8:
                    tempbdpresto.setC8(value);
                    return tempbdpresto;
                case 9:
                    tempbdpresto.setC9(value);
                    return tempbdpresto;
                case 10:
                    tempbdpresto.setC10(value);
                    return tempbdpresto;
                case 11:
                    tempbdpresto.setC11(value);
                    return tempbdpresto;
                case 12:
                    tempbdpresto.setC12(value);
                    return tempbdpresto;
                case 13:
                    tempbdpresto.setC13(value);
                    return tempbdpresto;
                case 14:
                    tempbdpresto.setC14(value);
                    return tempbdpresto;
                case 15:
                    tempbdpresto.setC15(value);
                    return tempbdpresto;
                case 16:
                    tempbdpresto.setC16(value);
                    return tempbdpresto;
                case 17:
                    tempbdpresto.setC17(value);
                    return tempbdpresto;
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Set value in parsing operation failed", e);
            return tempbdpresto;
        }
        return tempbdpresto;
    }


    private TempBDPrestoBase checkInstrumentQcResults(TempBDPrestoBase tempbdprestoBase,
                                                      List<TempBDPresto> tempbdprestos) {
        try {
            Boolean valid;
            TempBDPresto temp = null;
            for (TempBDPresto t : tempbdprestos) {
                if (t.getC0().contains("Instrument QC Results -")) {
                    temp = t;
                }
            }
            if (temp == null) {
                valid = false;
                tempbdprestoBase.setErrorMsg(tempbdprestoBase.getErrorMsg() + "--Instrument QC Results not found.--");
            } else {
                String[] InstrumentQCHeader = new String[7];
                InstrumentQCHeader[0] = tempbdprestos.get(temp.getId() + 1).getC0();
                InstrumentQCHeader[1] = tempbdprestos.get(temp.getId() + 1).getC1();
                InstrumentQCHeader[2] = tempbdprestos.get(temp.getId() + 1).getC2();
                InstrumentQCHeader[3] = tempbdprestos.get(temp.getId() + 1).getC3();
                InstrumentQCHeader[4] = tempbdprestos.get(temp.getId() + 1).getC4();
                InstrumentQCHeader[5] = tempbdprestos.get(temp.getId() + 1).getC5();
                InstrumentQCHeader[6] = tempbdprestos.get(temp.getId() + 1).getC6();
                if (InstrumentQCHeader.length == Constants.instrumentQcResultsTemplateFormat.length) {
                    if (Arrays.equals(InstrumentQCHeader, Constants.instrumentQcResultsTemplateFormat)) {
                        valid = true;
                        tempbdprestoBase.setInstrumentQcStartId(temp.getId() + 2);
                    } else {
                        valid = false;
                        tempbdprestoBase.setErrorMsg(
                                tempbdprestoBase.getErrorMsg() + "--Incorrect Instrument QC Results format.--");
                    }
                } else {
                    valid = false;
                    tempbdprestoBase.setErrorMsg(
                            tempbdprestoBase.getErrorMsg() + "--Incorrect Instrument QC Results format.--");
                }
            }
            tempbdprestoBase.setValidInstrumentQc(valid);
            return tempbdprestoBase;
        } catch (Exception e) {
            tempbdprestoBase.setValidInstrumentQc(false);
            tempbdprestoBase.setErrorMsg(tempbdprestoBase.getErrorMsg() + "Invalid Instrument QC Results");
            log.log(Level.SEVERE, "Check Instrument QC section operation failed", e);
            return tempbdprestoBase;
        }
    }

    private TempBDPrestoBase checkCD4ProcessCtrlResults(TempBDPrestoBase tempbdprestoBase,
                                                        List<TempBDPresto> tempbdprestos) {
        try {
            Boolean valid;
            TempBDPresto temp = null;
            for (TempBDPresto t : tempbdprestos) {
                if (t.getC0().contains("CD4 Process Control Results -")) {
                    temp = t;
                }
            }
            if (temp == null) {
                valid = false;
                tempbdprestoBase
                        .setErrorMsg(tempbdprestoBase.getErrorMsg() + "--CD4 Process Control Results not found.--");
            } else {
                String[] CD4ProcessHeader = new String[17];
                CD4ProcessHeader[0] = tempbdprestos.get(temp.getId() + 1).getC0();
                CD4ProcessHeader[1] = tempbdprestos.get(temp.getId() + 1).getC1();
                CD4ProcessHeader[2] = tempbdprestos.get(temp.getId() + 1).getC2();
                CD4ProcessHeader[3] = tempbdprestos.get(temp.getId() + 1).getC3();
                CD4ProcessHeader[4] = tempbdprestos.get(temp.getId() + 1).getC4();
                CD4ProcessHeader[5] = tempbdprestos.get(temp.getId() + 1).getC5();
                CD4ProcessHeader[6] = tempbdprestos.get(temp.getId() + 1).getC6();
                CD4ProcessHeader[7] = tempbdprestos.get(temp.getId() + 1).getC7();
                CD4ProcessHeader[8] = tempbdprestos.get(temp.getId() + 1).getC8();
                CD4ProcessHeader[9] = tempbdprestos.get(temp.getId() + 1).getC9();
                CD4ProcessHeader[10] = tempbdprestos.get(temp.getId() + 1).getC10();
                CD4ProcessHeader[11] = tempbdprestos.get(temp.getId() + 1).getC11();
                CD4ProcessHeader[12] = tempbdprestos.get(temp.getId() + 1).getC12();
                CD4ProcessHeader[13] = tempbdprestos.get(temp.getId() + 1).getC13();
                CD4ProcessHeader[14] = tempbdprestos.get(temp.getId() + 1).getC14();
                CD4ProcessHeader[15] = tempbdprestos.get(temp.getId() + 1).getC15();
                CD4ProcessHeader[16] = tempbdprestos.get(temp.getId() + 1).getC16();

                if (CD4ProcessHeader.length == Constants.cD4ProcessCtrlResultsTemplateFormat.length) {
                    if (Arrays.equals(CD4ProcessHeader, Constants.cD4ProcessCtrlResultsTemplateFormat)) {
                        valid = true;
                        tempbdprestoBase.setCd4ProcessStartId(temp.getId() + 2);
                    } else {
                        valid = false;
                        tempbdprestoBase.setErrorMsg(
                                tempbdprestoBase.getErrorMsg() + "--Incorrect CD4 Process Control Results format.--");
                    }
                } else {
                    valid = false;
                    tempbdprestoBase.setErrorMsg(
                            tempbdprestoBase.getErrorMsg() + "--Incorrect CD4 Process Control Results format.--");
                }
            }

            tempbdprestoBase.setValidCD4Process(valid);

            return tempbdprestoBase;
        } catch (Exception e) {
            tempbdprestoBase.setValidCD4Process(false);
            tempbdprestoBase.setErrorMsg(tempbdprestoBase.getErrorMsg() + "--Invalid CD4 Process Control Results--");
            log.log(Level.SEVERE, "Check CD4 Process section operation failed", e);
            return tempbdprestoBase;
        }

    }

    private TempBDPrestoBase checkHbProcessCtrlResults(TempBDPrestoBase tempbdprestoBase,
                                                       List<TempBDPresto> tempbdprestos) {
        try {
            Boolean valid;
            TempBDPresto temp = null;
            for (TempBDPresto t : tempbdprestos) {
                if (t.getC0().contains("Hb Process Control Results -")) {
                    temp = t;
                }
            }
            if (temp == null) {
                valid = false;
                tempbdprestoBase
                        .setErrorMsg(tempbdprestoBase.getErrorMsg() + "--Hb Process Control Results not found.--");
            } else {
                String[] HbProcessHeader = new String[14];
                HbProcessHeader[0] = tempbdprestos.get(temp.getId() + 1).getC0();
                HbProcessHeader[1] = tempbdprestos.get(temp.getId() + 1).getC1();
                HbProcessHeader[2] = tempbdprestos.get(temp.getId() + 1).getC2();
                HbProcessHeader[3] = tempbdprestos.get(temp.getId() + 1).getC3();
                HbProcessHeader[4] = tempbdprestos.get(temp.getId() + 1).getC4();
                HbProcessHeader[5] = tempbdprestos.get(temp.getId() + 1).getC5();
                HbProcessHeader[6] = tempbdprestos.get(temp.getId() + 1).getC6();
                HbProcessHeader[7] = tempbdprestos.get(temp.getId() + 1).getC7();
                HbProcessHeader[8] = tempbdprestos.get(temp.getId() + 1).getC8();
                HbProcessHeader[9] = tempbdprestos.get(temp.getId() + 1).getC9();
                HbProcessHeader[10] = tempbdprestos.get(temp.getId() + 1).getC10();
                HbProcessHeader[11] = tempbdprestos.get(temp.getId() + 1).getC11();
                HbProcessHeader[12] = tempbdprestos.get(temp.getId() + 1).getC12();
                HbProcessHeader[13] = tempbdprestos.get(temp.getId() + 1).getC13();
                if (HbProcessHeader.length == Constants.hbProcessCtrlResultsTemplateFormat.length) {
                    if (Arrays.equals(HbProcessHeader, Constants.hbProcessCtrlResultsTemplateFormat)) {
                        valid = true;
                        tempbdprestoBase.setHbProcessStartId(temp.getId() + 2);
                    } else {
                        valid = false;
                        tempbdprestoBase.setErrorMsg(
                                tempbdprestoBase.getErrorMsg() + "--Incorrect Hb Process Control Results format.--");
                    }
                } else {
                    valid = false;
                    tempbdprestoBase.setErrorMsg(
                            tempbdprestoBase.getErrorMsg() + "--Incorrect Hb Process Control Results format.--");
                }
            }
            tempbdprestoBase.setValidHbProcess(valid);
            return tempbdprestoBase;
        } catch (Exception e) {
            tempbdprestoBase.setValidHbProcess(false);
            tempbdprestoBase.setErrorMsg(tempbdprestoBase.getErrorMsg() + "--Invalid Hb Process Control Results--");
            log.log(Level.SEVERE, "Check HB Process section operation failed", e);
            return tempbdprestoBase;
        }
    }

    private TempBDPrestoBase checkPatientSampleResults(TempBDPrestoBase tempbdprestoBase,
                                                       List<TempBDPresto> tempbdprestos) {
        try {
            Boolean valid;
            TempBDPresto temp = null;
            for (TempBDPresto t : tempbdprestos) {
                if (t.getC0().contains("Patient Sample Results -")) {
                    temp = t;
                }
            }
            if (temp == null) {
                valid = false;
                tempbdprestoBase.setErrorMsg(tempbdprestoBase.getErrorMsg() + "Patient Sample Results not found");
            } else {
                String[] PatientSampleHeader = new String[12];
                PatientSampleHeader[0] = tempbdprestos.get(temp.getId() + 1).getC0();
                PatientSampleHeader[1] = tempbdprestos.get(temp.getId() + 1).getC1();
                PatientSampleHeader[2] = tempbdprestos.get(temp.getId() + 1).getC2();
                PatientSampleHeader[3] = tempbdprestos.get(temp.getId() + 1).getC3();
                PatientSampleHeader[4] = tempbdprestos.get(temp.getId() + 1).getC4();
                PatientSampleHeader[5] = tempbdprestos.get(temp.getId() + 1).getC5();
                PatientSampleHeader[6] = tempbdprestos.get(temp.getId() + 1).getC6();
                PatientSampleHeader[7] = tempbdprestos.get(temp.getId() + 1).getC7();
                PatientSampleHeader[8] = tempbdprestos.get(temp.getId() + 1).getC8();
                PatientSampleHeader[9] = tempbdprestos.get(temp.getId() + 1).getC9();
                PatientSampleHeader[10] = tempbdprestos.get(temp.getId() + 1).getC10();
                PatientSampleHeader[11] = tempbdprestos.get(temp.getId() + 1).getC11();
                if (PatientSampleHeader.length == Constants.patientSampleResultsTemplateFormat.length) {
                    if (Arrays.equals(PatientSampleHeader, Constants.patientSampleResultsTemplateFormat)) {
                        valid = true;
                        tempbdprestoBase.setPatientSampleStartId(temp.getId() + 2);
                    } else {
                        valid = false;
                        tempbdprestoBase.setErrorMsg(
                                tempbdprestoBase.getErrorMsg() + " Incorrect Patient Sample Results format");
                    }
                } else {
                    valid = false;
                    tempbdprestoBase.setErrorMsg(
                            tempbdprestoBase.getErrorMsg() + " Incorrect Patient Sample Results format.");
                }
            }
            tempbdprestoBase.setValidPatientSample(valid);
            return tempbdprestoBase;
        } catch (Exception e) {
            tempbdprestoBase.setValidPatientSample(false);
            tempbdprestoBase.setErrorMsg(tempbdprestoBase.getErrorMsg() + " Invalid Patient Sample Results");
            log.log(Level.SEVERE, "Check Patient sample result section operation failed", e);
            return tempbdprestoBase;
        }
    }

    private void importPrestoToDatabase(List<TempBDPresto> fToImport, TempBDPrestoBase prestoBase) {
        Integer totalImported = 0;
        boolean refresh = false;

        Optional<Device> optionalDevice = deviceRepository.getDeviceBySerialNumber(prestoBase.getDeviceCode());
        Device device;
        if (optionalDevice.isEmpty()) {
            device = new Device();
            device.setSerialNumber(prestoBase.getDeviceCode());
            device.setDeviceType(DeviceType.BD_FACSPRESTO);
            Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
            if (nodeConfiguration.isPresent())
                device.setTestingPoint(nodeConfiguration.get().getFacility());
            device = deviceRepository.save(device);
            refresh = true;
            log.info("New facspresto device " + device.getSerialNumber() + " registered");
        } else {
            device = optionalDevice.get();
            if (!device.getTestingPoint().equalsIgnoreCase(prestoBase.getLabName())) {
                device.setTestingPoint(prestoBase.getLabName());
                device = deviceRepository.save(device);
            }
        }
        try {
            if (prestoBase.getValidInstrumentQc()) {
                List<FacsprestoInstrumentQc> instrumentQC = this.createInstrumentQCFromPrestoFile(fToImport,
                        prestoBase);
                Integer instrumentImported = facsprestoInstrumentQcRepository.saveAll(instrumentQC).size();
                if (instrumentImported != null) {
                    totalImported += instrumentImported;
                }
            }
            if (prestoBase.getValidCD4Process()) {
                List<FacsprestoCd4Control> cd4ControlProcess = this
                        .createCD4ControlProcessFromPrestoFile(fToImport, prestoBase);
                List<FacsprestoCd4Control> cd4Imported = facsprestoCd4ControlRepository.saveAll(cd4ControlProcess);
                if (cd4Imported != null) {
                    totalImported += cd4Imported.size();
                    if (!cd4Imported.isEmpty()) {
                        for (FacsprestoCd4Control cd4CtrlProcess : cd4Imported) {
                            try {
                                deviceService.updateCartridgeByLotId(cd4CtrlProcess.getReagentLotId(),
                                        device);
                            } catch (Exception e) {
                                log.log(Level.SEVERE, e.getMessage(), e);
                            }
                            deviceService.updateUsedCartridgeHistoric(device.getSerialNumber(),
                                    cd4CtrlProcess.getRunDatetime().getMonthValue(), cd4CtrlProcess.getRunDatetime().getYear());
                        }
                    }
                }
            }
            if (prestoBase.getValidHbProcess()) {
                List<FacsprestoHbControl> hbControlProcess = this
                        .createHbControlProcessFromPrestoFile(fToImport, prestoBase);
                List<FacsprestoHbControl> hbImported = facsprestoHbControlRepository.saveAll(hbControlProcess);
                if (hbImported != null) {
                    totalImported += hbImported.size();
                    if (!hbImported.isEmpty()) {
                        for (FacsprestoHbControl facsPrestoHbControlProcess : hbImported) {
                            try {
                                deviceService.updateCartridgeByLotId(facsPrestoHbControlProcess.getReagentLotId(),
                                        device);
                            } catch (Exception e) {
                                log.log(Level.SEVERE, e.getMessage(), e);
                            }
                            this.deviceService.updateUsedCartridgeHistoric(device.getSerialNumber(),
                                    facsPrestoHbControlProcess.getRunDatetime().getMonthValue(), facsPrestoHbControlProcess.getRunDatetime().getYear());
                        }
                    }
                }
            }
            if (prestoBase.getValidPatientSample()) {
                List<FacsprestoPatientSample> patientSample = this.createPatientSResultFromPrestoFile(fToImport,
                        prestoBase);
                List<FacsprestoPatientSample> patientSampleImported = facsprestoPatientSampleRepository.saveAll(patientSample);
                if (patientSampleImported != null) {
                    totalImported += patientSampleImported.size();

                    if (!patientSampleImported.isEmpty()) {
                        for (FacsprestoPatientSample patientSResult : patientSampleImported) {
                            try {
                                this.deviceService.updateCartridgeByLotId(patientSResult.getReagentLotId(),
                                        device);
                            } catch (Exception e) {
                                log.log(Level.SEVERE, e.getMessage(), e);
                            }
                            this.deviceService.updateUsedCartridgeHistoric(device.getSerialNumber(),
                                    patientSResult.getRunDatetime().getMonthValue(), patientSResult.getRunDatetime().getYear());
                        }
                    }
                }
            }

            if (totalImported > 0) {
                patientSampleFacsprestoAPIService.postIndividualTests();
                if (device != null)

                    this.deviceService.verifyRemainingInventoryByDevice(device);
                cartridgeInventoryAPIService.sendCartridgeInventory();
                MessageNotification messageNotification = messageNotificationRepository.findByTriggerName(NotificationMessage.DATA_SENT_TO_NODE.getTriggerName());
                if (messageNotification.getStatus()) {
                    String message = messageNotification.getText();
                    Broadcaster.broadcast(message.replace("#deviceCode", device.getSerialNumber()), NotificationVariant.LUMO_SUCCESS);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Import facspresto file to Database operation failed", e);
        }
        if (refresh) {
            Broadcaster.broadcast("reload", null);
            Broadcaster.broadcast("Device " + device.getDeviceType().getScreenName() + " registered in the node",
                    NotificationVariant.LUMO_SUCCESS);
        }

    }

    private List<FacsprestoInstrumentQc> createInstrumentQCFromPrestoFile(List<TempBDPresto> fToImport,
                                                                          TempBDPrestoBase prestoBase) {
        List<FacsprestoInstrumentQc> instrumentQC = new ArrayList<>();
        try {
            for (TempBDPresto tempBDPresto : fToImport) {
                if (tempBDPresto.getId() >= prestoBase.getInstrumentQcStartId()) {
                    if (tempBDPresto.getC0().isEmpty()) {
                        break;
                    }
                    FacsprestoInstrumentQc insQC = new FacsprestoInstrumentQc();
                    insQC.setVersion(prestoBase.getVersion());
                    insQC.setDeviceCode(prestoBase.getDeviceCode());
                    insQC.setLabName(prestoBase.getLabName());
                    insQC.setRunId(tempBDPresto.getC0());
                    insQC.setRunDatetime(DateUtils.parseDateTime(cleanDate(tempBDPresto.getC1())));
                    insQC.setOperator(tempBDPresto.getC2());
                    insQC.setNormalCount(Integer.parseInt(cleanNumber(tempBDPresto.getC3())));
                    insQC.setLowCount(Integer.parseInt(cleanNumber(tempBDPresto.getC4())));
                    insQC.setPass(tempBDPresto.getC5());
                    insQC.setErrorCode(tempBDPresto.getC6());
                    instrumentQC.add(insQC);
                }
            }
            return instrumentQC;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Create Instrument QC Process operation failed", e);
            return instrumentQC;
        }

    }

    private List<FacsprestoCd4Control> createCD4ControlProcessFromPrestoFile(List<TempBDPresto> fToImport,
                                                                             TempBDPrestoBase prestoBase) {
        List<FacsprestoCd4Control> cd4ControlProcess = new ArrayList<>();
        try {
            for (TempBDPresto tempBDPresto : fToImport) {
                if (tempBDPresto.getId() >= prestoBase.getCd4ProcessStartId()) {
                    if (tempBDPresto.getC0().isEmpty()) {
                        break;
                    }
                    FacsprestoCd4Control cd4CtrlProcess = new FacsprestoCd4Control();
                    cd4CtrlProcess.setVersion(prestoBase.getVersion());
                    cd4CtrlProcess.setDeviceCode(prestoBase.getDeviceCode());
                    cd4CtrlProcess.setLabName(prestoBase.getLabName());

                    cd4CtrlProcess.setRunId(tempBDPresto.getC0());
                    cd4CtrlProcess.setRunDatetime(DateUtils.parseDateTime(cleanDate(tempBDPresto.getC1())));

                    cd4CtrlProcess.setOperator(tempBDPresto.getC2());
                    cd4CtrlProcess.setReagentLotId(tempBDPresto.getC3());
                    cd4CtrlProcess
                            .setReagentLotExpDate(DateUtils.parseDate(cleanDate(tempBDPresto.getC4())));
                    cd4CtrlProcess.setProcessLotId(tempBDPresto.getC5());
                    cd4CtrlProcess
                            .setProcessLotExpDate(DateUtils.parseDate(cleanDate(tempBDPresto.getC6())));
                    cd4CtrlProcess.setLevel(tempBDPresto.getC7());
                    cd4CtrlProcess.setExpCd4Lower(Double.parseDouble(cleanNumber(tempBDPresto.getC8())));
                    cd4CtrlProcess.setExpCd4Upper(Double.parseDouble(cleanNumber(tempBDPresto.getC9())));
                    cd4CtrlProcess
                            .setExpPercentCd4Lower(Double.parseDouble(cleanNumber(tempBDPresto.getC10())));
                    cd4CtrlProcess
                            .setExpPercentCd4Upper(Double.parseDouble(cleanNumber(tempBDPresto.getC11())));
                    cd4CtrlProcess.setReagentqcPperF(tempBDPresto.getC12());
                    cd4CtrlProcess.setCd4(Integer.parseInt(cleanNumber(tempBDPresto.getC13())));
                    cd4CtrlProcess.setPercentCD4(Double.parseDouble(cleanNumber(tempBDPresto.getC14())));
                    cd4CtrlProcess.setPass(tempBDPresto.getC15());
                    cd4CtrlProcess.setErrorCode(tempBDPresto.getC16());

                    cd4ControlProcess.add(cd4CtrlProcess);
                }
            }
            return cd4ControlProcess;
        } catch (Exception e) {
            log.log(Level.SEVERE, "cd4 control Process operation failed", e);
            return cd4ControlProcess;
        }
    }

    private List<FacsprestoHbControl> createHbControlProcessFromPrestoFile(List<TempBDPresto> fToImport,
                                                                           TempBDPrestoBase prestoBase) {
        List<FacsprestoHbControl> hbControlProcess = new ArrayList<>();
        try {
            for (TempBDPresto tempBDPresto : fToImport) {
                if (tempBDPresto.getId() >= prestoBase.getHbProcessStartId()) {
                    if (tempBDPresto.getC0().isEmpty()) {
                        break;
                    }
                    FacsprestoHbControl HbCtrlProcess = new FacsprestoHbControl();
                    HbCtrlProcess.setVersion(prestoBase.getVersion());
                    HbCtrlProcess.setDeviceCode(prestoBase.getDeviceCode());
                    HbCtrlProcess.setLabName(prestoBase.getLabName());
                    HbCtrlProcess.setRunId(tempBDPresto.getC0());
                    HbCtrlProcess.setRunDatetime(DateUtils.parseDateTime(cleanDate(tempBDPresto.getC1())));
                    HbCtrlProcess.setOperator(tempBDPresto.getC2());
                    HbCtrlProcess.setReagentLotId(tempBDPresto.getC3());
                    HbCtrlProcess
                            .setReagentLotExpDate(DateUtils.parseDate(cleanDate(tempBDPresto.getC4())));
                    HbCtrlProcess.setProcessLotId(tempBDPresto.getC5());
                    HbCtrlProcess
                            .setProcessLotExpDate(DateUtils.parseDate(cleanDate(tempBDPresto.getC6())));
                    HbCtrlProcess.setLevel(tempBDPresto.getC7());
                    HbCtrlProcess.setExpHbLower(Double.parseDouble(cleanNumber(tempBDPresto.getC8())));
                    HbCtrlProcess.setExpHbUpper(Double.parseDouble(cleanNumber(tempBDPresto.getC9())));
                    HbCtrlProcess.setReagentqcPperF(tempBDPresto.getC10());
                    HbCtrlProcess.setHbgPerDl(Double.parseDouble(cleanNumber(tempBDPresto.getC11())));
                    HbCtrlProcess.setPass(tempBDPresto.getC12());
                    HbCtrlProcess.setErrorCode(tempBDPresto.getC13());
                    hbControlProcess.add(HbCtrlProcess);
                }
            }
            return hbControlProcess;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Create HB Control process operation failed", e);
            return hbControlProcess;
        }

    }

    private List<FacsprestoPatientSample> createPatientSResultFromPrestoFile(List<TempBDPresto> fToImport,
                                                                             TempBDPrestoBase prestoBase) {
        List<FacsprestoPatientSample> patientSampleList = new ArrayList<>();
        try {
            for (TempBDPresto tempBDPresto : fToImport) {
                if (tempBDPresto.getId() >= prestoBase.getPatientSampleStartId()) {
                    if (tempBDPresto.getC0().isEmpty()) {
                        break;
                    }
                    FacsprestoPatientSample patientSResult = new FacsprestoPatientSample();
                    patientSResult.setVersion(prestoBase.getVersion());
                    patientSResult.setDeviceCode(prestoBase.getDeviceCode());
                    patientSResult.setLabName(prestoBase.getLabName());
                    patientSResult.setRunId(tempBDPresto.getC0());
                    patientSResult.setRunDatetime(DateUtils.parseDateTime(cleanDate(tempBDPresto.getC1())));
                    patientSResult.setOperator(tempBDPresto.getC2());
                    patientSResult.setReagentLotId(tempBDPresto.getC3());
                    patientSResult
                            .setReagentLotExpDate(DateUtils.parseDate(cleanDate(tempBDPresto.getC4())));
                    String patientId = " ";
                    String text = tempBDPresto.getC5().replaceAll("-", "");
                    if (StringUtils.isNumericSpace(text)) {
                        patientId = tempBDPresto.getC5();
                    }
                    patientSResult.setPatientId(patientId);
                    patientSResult.setInstQcPassed(tempBDPresto.getC6());
                    patientSResult.setReagentQcPassed(tempBDPresto.getC7());
                    patientSResult.setCd4(cleanNumber(tempBDPresto.getC8()));
                    patientSResult.setPercentCd4(Double.parseDouble(cleanNumber(tempBDPresto.getC9())));
                    patientSResult.setHb(Double.parseDouble(cleanNumber(tempBDPresto.getC10())));
                    patientSResult.setErrorCode(tempBDPresto.getC11());
                    patientSampleList.add(patientSResult);
                }
            }
            return patientSampleList;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Create Patient Sample Process operation failed", e);
            return patientSampleList;
        }
    }

    private String cleanNumber(String source) {
        String str = source.replace("[", "");
        str = str.replace("]", "");
        return str;
    }

    private String cleanDate(String source) {
        String str = source.replace("!", "");
        return str;
    }


    @Override
    public void exportBDPrestoFile(File targetFolder, Constants.PRESTO_CATEGORY category) throws IOException {
        List<FacsprestoInstrumentQc> instrumentQcList = new ArrayList<>();
        List<FacsprestoCd4Control> cd4ControlList = new ArrayList<>();
        List<FacsprestoHbControl> hbControlList = new ArrayList<>();
        List<FacsprestoPatientSample> patientSampleList = new ArrayList<>();
        String deviceId = "";
        switch (category) {
            case INSTRUMENT_QC:
                instrumentQcList = facsprestoInstrumentQcRepository.findAllByIsExportedFalse();
                if (instrumentQcList.isEmpty())
                    return;
                deviceId = instrumentQcList.get(0).getDeviceCode();
                break;
            case CD4_CONTROL_PROCESS:
                cd4ControlList = facsprestoCd4ControlRepository.findAllByIsExportedFalse();
                if (cd4ControlList.isEmpty())
                    return;
                deviceId = cd4ControlList.get(0).getDeviceCode();
                break;
            case HB_CONTROL_PROCESS:
                hbControlList = facsprestoHbControlRepository.findAllByIsExportedFalse();
                if (hbControlList.isEmpty())
                    return;
                deviceId = hbControlList.get(0).getDeviceCode();
                break;
            case PATIENT_SAMPLE:
                patientSampleList = facsprestoPatientSampleRepository.findAllByIsExportedFalse();
                if (patientSampleList.isEmpty())
                    return;
                deviceId = patientSampleList.get(0).getDeviceCode();
                break;
            default:
                break;
        }

        String NEW_LINE_SEPARATOR = "\n";
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();
        File expFile = null;
        Optional<NodeHealthStatistics> nodeHealthStatistics = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty() || !nodeConfiguration.get().getIsExported()) {
            log.warning("Node configuration isn't exported yet. " + category + "  Data won't get exported.");
            return;
        }

        String macId = "";
        if (nodeHealthStatistics.isPresent())
            macId = nodeHealthStatistics.get().getMacId();

        String fName = category + "-" + nodeConfiguration.get().getFacility();
        String fUniqueKey = new SimpleDateFormat("yyMMddhhmmssSSS").format(new Date());
        expFile = new File(targetFolder + File.separator + fName + "-"
                + deviceId + "(" + fUniqueKey + ").csv");
        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        // initialize CSVPrinter object
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
        // write header
        switch (category) {
            case INSTRUMENT_QC:
                csvFilePrinter.printRecord(Constants.instrumentQcResultsTemplateFormat_H);
                break;
            case CD4_CONTROL_PROCESS:
                csvFilePrinter.printRecord(Constants.cD4ProcessCtrlResultsTemplateFormat_H);
                break;
            case HB_CONTROL_PROCESS:
                csvFilePrinter.printRecord(Constants.hbProcessCtrlResultsTemplateFormat_H);
                break;
            case PATIENT_SAMPLE:
                csvFilePrinter.printRecord(Constants.patientSampleResultsTemplateFormat_H);
                break;
            default:
                break;
        }

        switch (category) {
            case INSTRUMENT_QC:
                for (FacsprestoInstrumentQc data : instrumentQcList) {
                    List<Object> tempData = writeInstrumentQcResults(data, macId, nodeConfiguration.get());
                    csvFilePrinter.printRecord(tempData);
                }
                break;
            case CD4_CONTROL_PROCESS:
                for (FacsprestoCd4Control data : cd4ControlList) {
                    List<Object> tempData = writeCd4ProcessResults(data, macId, nodeConfiguration.get());
                    csvFilePrinter.printRecord(tempData);
                }
                break;
            case HB_CONTROL_PROCESS:
                for (FacsprestoHbControl data : hbControlList) {
                    List<Object> tempData = writeHbProcessResults(data, macId, nodeConfiguration.get());
                    csvFilePrinter.printRecord(tempData);
                }
                break;
            case PATIENT_SAMPLE:
                for (FacsprestoPatientSample data : patientSampleList) {
                    List<Object> tempData = writePatientResults(data, macId, nodeConfiguration.get());
                    csvFilePrinter.printRecord(tempData);
                }
                break;
            default:
                break;
        }

        for (FacsprestoInstrumentQc test : instrumentQcList) {
            test.setIsExported(true);
            facsprestoInstrumentQcRepository.save(test);
        }
        for (FacsprestoPatientSample test : patientSampleList) {
            test.setIsExported(true);
           facsprestoPatientSampleRepository.save(test);
        }
        for (FacsprestoHbControl test : hbControlList) {
            test.setIsExported(true);
            facsprestoHbControlRepository.save(test);
        }
        for (FacsprestoCd4Control test : cd4ControlList) {
            test.setIsExported(true);
            facsprestoCd4ControlRepository.save(test);
        }
        log.info(category + " file was created successfully");
    }

    private List<Object> writeCd4ProcessResults(FacsprestoCd4Control data, String macId, NodeConfiguration nodeConfiguration) {
        List<Object> tempData = new ArrayList<>();
        try {
            tempData.add(macId);
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(nodeConfiguration.getCountry());
            tempData.add(nodeConfiguration.getProvince());
            tempData.add(nodeConfiguration.getDistrict());
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(data.getDeviceCode());
            tempData.add(data.getRunId());
            tempData.add(data.getRunDatetime().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getOperator());
            tempData.add(data.getReagentLotId());
            tempData.add(data.getReagentLotExpDate().format(DateUtils.DATE_FORMAT));
            tempData.add(data.getProcessLotId());
            tempData.add(data.getProcessLotExpDate().format(DateUtils.DATE_FORMAT));
            tempData.add(data.getLevel());
            tempData.add(data.getExpCd4Lower());
            tempData.add(data.getExpCd4Upper());
            tempData.add(data.getExpPercentCd4Lower());
            tempData.add(data.getExpPercentCd4Upper());
            tempData.add(data.getReagentqcPperF());
            tempData.add(data.getCd4());
            tempData.add(data.getPercentCD4());
            tempData.add(data.getPass());
            tempData.add(data.getErrorCode());
            return tempData;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in CsvFileWriter for --" + data.getDeviceCode()
                    + "-- operation failed", e);
            return tempData;
        }
    }

    private List<Object> writeHbProcessResults(FacsprestoHbControl data, String macId, NodeConfiguration nodeConfiguration) {
        List<Object> tempData = new ArrayList<>();
        try {
            tempData.add(macId);
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(nodeConfiguration.getCountry());
            tempData.add(nodeConfiguration.getProvince());
            tempData.add(nodeConfiguration.getDistrict());
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(data.getDeviceCode());
            tempData.add(data.getRunId());
            tempData.add(data.getRunDatetime().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getOperator());
            tempData.add(data.getReagentLotId());
            tempData.add(data.getReagentLotExpDate().format(DateUtils.DATE_FORMAT));
            tempData.add(data.getProcessLotId());
            tempData.add(data.getProcessLotExpDate().format(DateUtils.DATE_FORMAT));
            tempData.add(data.getLevel());
            tempData.add(data.getExpHbLower());
            tempData.add(data.getExpHbUpper());
            tempData.add(data.getReagentqcPperF());
            tempData.add(data.getHbgPerDl());
            tempData.add(data.getPass());
            tempData.add(data.getErrorCode());
            return tempData;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in CsvFileWriter for --" + data.getDeviceCode()
                    + "-- operation failed", e);
            return tempData;
        }
    }

    private List<Object> writeInstrumentQcResults(FacsprestoInstrumentQc data, String macId, NodeConfiguration
            nodeConfiguration) {
        List<Object> tempData = new ArrayList<>();
        try {
            tempData.add(macId);
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(nodeConfiguration.getCountry());
            tempData.add(nodeConfiguration.getProvince());
            tempData.add(nodeConfiguration.getDistrict());
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(data.getDeviceCode());
            tempData.add(data.getRunId());
            tempData.add(data.getRunDatetime().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getOperator());
            tempData.add(data.getNormalCount());
            tempData.add(data.getLowCount());
            tempData.add(data.getPass());
            tempData.add(data.getErrorCode());
            return tempData;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in CsvFileWriter for --" + data.getDeviceCode()
                    + "-- operation failed", e);
            return tempData;
        }
    }

    private List<Object> writePatientResults(FacsprestoPatientSample data, String macId, NodeConfiguration
            nodeConfiguration) {
        List<Object> tempData = new ArrayList<>();
        try {
            tempData.add(macId);
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(nodeConfiguration.getCountry());
            tempData.add(nodeConfiguration.getProvince());
            tempData.add(nodeConfiguration.getDistrict());
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(data.getDeviceCode());
            tempData.add(data.getRunId());
            tempData.add(data.getRunDatetime().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getOperator());
            tempData.add(data.getReagentLotId());
            tempData.add(data.getReagentLotExpDate().format(DateUtils.DATE_FORMAT));
            tempData.add(data.getPatientId());
            tempData.add(data.getInstQcPassed());
            tempData.add(data.getReagentQcPassed());
            tempData.add(data.getCd4());
            tempData.add(data.getPercentCd4());
            tempData.add(data.getHb());
            tempData.add(data.getErrorCode());
            tempData.add(data.getSysLogdate().format(DateUtils.TIME_FORMAT));
            tempData.add(data.getNascopTimestamp().format(DateUtils.TIME_FORMAT));
            return tempData;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in CsvFileWriter for --" + data.getDeviceCode()
                    + "-- operation failed", e);
            return tempData;
        }
    }


}
