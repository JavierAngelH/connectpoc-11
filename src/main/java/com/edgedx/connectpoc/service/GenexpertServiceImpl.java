package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.entity.*;
import com.edgedx.connectpoc.model.DeviceType;
import com.edgedx.connectpoc.model.GeneXpertData;
import com.edgedx.connectpoc.model.GeneXpertGeneralData;
import com.edgedx.connectpoc.model.NotificationMessage;
import com.edgedx.connectpoc.repository.*;
import com.edgedx.connectpoc.utils.Constants;
import com.edgedx.connectpoc.utils.DateUtils;
import com.edgedx.connectpoc.views.Broadcaster;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.csv.CSVReader;
import com.jcraft.jsch.*;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.Cleanup;
import lombok.extern.java.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

@Service
@Log
public class GenexpertServiceImpl implements GenexpertService {

    @Autowired
    PropertiesService propertiesService;
    @Autowired
    GenexpertRepository genexpertRepository;
    @Autowired
    DeviceRepository deviceRepository;
    @Autowired
    DeviceService deviceService;
    @Autowired
    MessageNotificationRepository messageNotificationRepository;
    @Autowired
    GenexpertProbecheckDetailRepository genexpertProbecheckDetailRepository;
    @Autowired
    GenexpertTestAnalyteRepository genexpertTestAnalyteRepository;
    @Autowired
    FtpGenexpertInfoRepository ftpGenexpertInfoRepository;
    @Autowired
    GenexpertAPIService genexpertAPIService;
    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;
    @Autowired
    NodeHealthStatisticsRepository nodeHealthStatisticsRepository;

    private static JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

    private static PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
            .setProvider("BC").build("brainchex2019".toCharArray());

    public void decryptGeneXpertFiles(File sourceFolder) throws IOException {
        // if provider is not present, add it
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            // insert at specific position
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
        }
        // get downloaded files with .csv extension
        File[] files = sourceFolder.listFiles((dir, name) -> name.endsWith(".enc"));
        // if files are found; loop through files
        if (files != null && files.length > 0) {
            PEMParser pr = null;
            ByteArrayOutputStream outputStream = null;
            try {
                String privateKeyFile = propertiesService.getFullPropertyValue("genexpert.encryptation.key");
                pr = openPEMResource(privateKeyFile);
                Object o = pr.readObject();
                if (!((o instanceof PEMKeyPair) || (o instanceof PEMEncryptedKeyPair))) {
                    log.log(Level.SEVERE, "Didn't find OpenSSL key");
                }
                KeyPair kp = (o instanceof PEMEncryptedKeyPair)
                        ? converter.getKeyPair(((PEMEncryptedKeyPair) o).decryptKeyPair(decProv))
                        : converter.getKeyPair((PEMKeyPair) o);
                RSAPrivateCrtKey privKey = (RSAPrivateCrtKey) kp.getPrivate();
                OAEPEncoding eng = new OAEPEncoding(new RSAEngine());
                BigInteger mod = privKey.getModulus();
                BigInteger exp = privKey.getPrivateExponent();
                RSAKeyParameters keyParams = new RSAKeyParameters(true, mod, exp);
                eng.init(false, keyParams);
                for (File file : files) {
                    String encryptedData = getFileAsString(file.getAbsolutePath());
                    byte[] decoded = DatatypeConverter.parseBase64Binary(encryptedData);
                    int length = encryptedData.length();
                    int blockSize = eng.getInputBlockSize();
                    outputStream = new ByteArrayOutputStream();
                    for (int chunkPosition = 0; chunkPosition < length; chunkPosition += blockSize) {
                        int chunkSize = Math.min(blockSize, length - chunkPosition);
                        try {
                            outputStream.write(eng.processBlock(decoded, chunkPosition, chunkSize));
                        } catch (Exception e) {
                            log.log(Level.SEVERE, e.getMessage());
                        }
                    }
                    writeToFile(outputStream, file.getAbsolutePath());
                    String targetDirectory = propertiesService.getFullPropertyValue("genexpert.backup.folder");
                    try {
                        FileUtils.moveFileToDirectory(file, new File(targetDirectory), false);
                    } catch (Exception e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        FileUtils.deleteQuietly(file);
                    }
                }
                Map<String, Integer> results = this.importGeneXpertFile(sourceFolder);
                FtpGenexpertInfo info = new FtpGenexpertInfo();
                info.setTotalTests(results.get("tests"));
                ftpGenexpertInfoRepository.save(info);


            } catch (Exception e) {
                log.log(Level.SEVERE, "GeneXpert file decryption operation failed", e);
            } finally {
                if (pr != null)
                    try {
                        pr.close();
                    } catch (IOException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                    }
                if (outputStream != null)
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                    }
            }
        }
    }

    private PEMParser openPEMResource(String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        InputStreamReader isr = new InputStreamReader(fis);
        Reader fRd = new BufferedReader(isr);
        return new PEMParser(fRd);
    }

    private void writeToFile(ByteArrayOutputStream outputStream, String filepath) throws IOException {
        String newFile = filepath.replace(".enc", ".csv");
        File file = new File(newFile);
        @Cleanup FileOutputStream fop = new FileOutputStream(file);
        // if file doesn't exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        // get the content in bytes
        byte[] contentInBytes = outputStream.toByteArray();
        fop.write(contentInBytes);
        fop.flush();
    }

    private String getFileAsString(String path) throws IOException {
        StringBuilder fileData = new StringBuilder(1000);
        @Cleanup FileReader fileReader = new FileReader(path);
        @Cleanup BufferedReader reader = new BufferedReader(fileReader);
        char[] buf = new char[1024];
        int numRead;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        return fileData.toString();
    }

    private Map<String, Integer> importGeneXpertFile(File sourceFolder) throws IOException {
        Map<String, Integer> answer = new HashMap<>(2);
        int totalFiles = 0;
        int totalTests = 0;
        int insertedTests = 0;
        Device device = null;
        boolean refresh = false;

        for (final File fileEntry : sourceFolder.listFiles()) {
            if (fileEntry.getName().endsWith(".txt")) {
                FileUtils.deleteQuietly(fileEntry);
            }
        }
        // get downloaded files with .csv extension
        File[] files = sourceFolder.listFiles((dir, name) -> name.endsWith(".csv"));
        // if files are found; loop through files
        if (files != null) {

            for (File file : files) {
                // check if file is node stat file
                boolean validGeneXpertFile = false;
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                CSVReader reader = new CSVReader(isr);
                ICommonsList<String> firstLine = reader.readNext();
                if (firstLine != null)
                    if (firstLine.size() > 1) {
                        if (firstLine.get(1).contains("GeneXpert")) {
                            validGeneXpertFile = true;
                        }
                    }
                if (!validGeneXpertFile) {
                    isr = new InputStreamReader(fis, StandardCharsets.UTF_16LE);
                    reader = new CSVReader(isr);
                    firstLine = reader.readNext();
                    if (firstLine != null)
                        if (firstLine.size() > 1) {
                            if (firstLine.get(1).contains("GeneXpert")) {
                                validGeneXpertFile = true;
                            }
                        }
                }

                GeneXpertGeneralData generalData = new GeneXpertGeneralData();
                List<GeneXpertData> testResults = new ArrayList<>();
                if (validGeneXpertFile) {
                    totalFiles++;
                    ICommonsList<String> line;
                    LocalDateTime startTime = null;
                    LocalDateTime endTime = null;
                    String sampleId = " ";
                    String patientId = " ";
                    String testType = null;
                    String sampleType = null;
                    String status = null;
                    String notes = null;
                    String errorStatus = null;
                    String reagentLotId = null;
                    LocalDate expirationDate = null;
                    String cartridgeIndex = null;
                    String cartridgeSN = null;
                    String moduleName = null;
                    String moduleSN = null;
                    String instrumentSN = null;
                    String softwareVersion = null;
                    String result = null;
                    String testDisclaimer = null;
                    String meltPeaks = null;
                    String error = null;
                    String messages = null;
                    String history = null;
                    String detail = null;
                    while ((line = reader.readNext()) != null) { // start
                        // loop to store common data
                        String name = line.get(0);
                        switch (name) {
                            case "System Name":
                                if (line.size() > 1) {
                                    generalData.setDeviceName(line.get(1));
                                }
                                break;
                            case "Exported Date":
                                if (line.size() > 1) {
                                    String dateString = line.get(1);
                                    LocalDateTime fileDate = DateUtils.parseGeneXpert(dateString);
                                    LocalDateTime exportedDate;
                                    if (fileDate != null) {
                                        if (fileDate.isBefore(LocalDateTime.now()))
                                            exportedDate = fileDate;
                                        else
                                            exportedDate = DateUtils.parseGeneXpert2(dateString);
                                        generalData.setExportedDate(exportedDate);
                                    }
                                }
                                break;
                            case "Report User Name":
                                if (line.size() > 1) {
                                    generalData.setUser(line.get(1));
                                }
                                break;
                            case "Assay":
                                if (line.size() > 1) {
                                    generalData.setAssay(line.get(1));
                                }
                                break;
                            case "Assay Version":
                                if (line.size() > 1) {
                                    generalData.setAssayVersion(line.get(1));
                                }
                                break;
                            case "Assay Type":
                                if (line.size() > 1) {
                                    generalData.setAssayType(line.get(1));
                                }
                                break;
                            case "Need Lot Specific Parameters":
                                if (line.size() > 1) {
                                    generalData.setSpecificParameters(line.get(1));
                                }
                                break;
                            case "Reagent Lot Number":
                                if (line.size() > 1) {
                                    generalData.setReagentLotNumber(line.get(1));
                                }
                                break;
                            case "Assay Disclaimer":
                                if (line.size() > 1) {
                                    generalData.setAssayDisclaimer(line.get(1));
                                }
                                break;
                            case "Sample ID":
                                if (line.size() > 1) {
                                    sampleId = line.get(1);
                                }
                                break;
                            case "Patient ID":
                                if (line.size() > 1) {
                                    String text = line.get(1).replaceAll("-", "");
                                    if (StringUtils.isNumericSpace(text)) {
                                        patientId = line.get(1);
                                    }
                                }
                                break;
                            case "Test Type":
                                if (line.size() > 1) {
                                    testType = line.get(1);
                                }
                                break;
                            case "Sample Type":
                                if (line.size() > 1) {
                                    sampleType = line.get(1);
                                }
                                break;
                            case "Status":
                                if (line.size() > 1) {
                                    status = line.get(1);
                                }
                                break;
                            case "Notes":
                                if (line.size() > 1) {
                                    notes = line.get(1);
                                }
                                break;
                            case "Start Time":
                                if (line.size() > 1) {
                                    String dateString = line.get(1);
                                    LocalDateTime fileDate = DateUtils.parseGeneXpertTime(dateString);
                                    if (fileDate != null) {
                                        if (fileDate.isBefore(LocalDateTime.now()))
                                            startTime = fileDate;
                                        else
                                            startTime = DateUtils.parseGeneXpertTime2(dateString);
                                    }
                                }
                                break;
                            case "End Time":
                                if (line.size() > 1) {
                                    String dateString = line.get(1);
                                    LocalDateTime fileDate = DateUtils.parseGeneXpertTime(dateString);
                                    if (fileDate != null) {
                                        if (fileDate.isBefore(LocalDateTime.now()))
                                            endTime = fileDate;
                                        else
                                            endTime = DateUtils.parseGeneXpertTime2(dateString);
                                    }
                                }
                                break;
                            case "Error Status":
                                if (line.size() > 1) {
                                    errorStatus = line.get(1);
                                }
                                break;
                            case "Reagent Lot ID":
                                if (line.size() > 1) {
                                    reagentLotId = line.get(1);
                                }
                                break;
                            case "Expiration Date":
                                if (line.size() > 1) {
                                    String dateString = line.get(1);
                                    LocalDate fileDate = DateUtils.parseGeneXpertShort(dateString);
                                    if (fileDate != null) {
                                        if (fileDate.isAfter(LocalDate.now()))
                                            expirationDate = fileDate;
                                        else
                                            expirationDate = DateUtils.parseGeneXpertShort2(dateString);
                                    }
                                }
                                break;
                            case "Cartridge Index":
                                if (line.size() > 1) {
                                    cartridgeIndex = line.get(1);
                                }
                                break;
                            case "Cartridge S/N":
                                if (line.size() > 1) {
                                    cartridgeSN = line.get(1);
                                }
                                break;
                            case "Module Name":
                                if (line.size() > 1) {
                                    moduleName = line.get(1);
                                }
                                break;
                            case "Module S/N":
                                if (line.size() > 1) {
                                    moduleSN = line.get(1);
                                }
                                break;
                            case "Instrument S/N":
                                if (line.size() > 1) {
                                    instrumentSN = line.get(1);
                                }
                                break;
                            case "S/W Version":
                                if (line.size() > 1) {
                                    softwareVersion = line.get(1);
                                }
                                break;
                            case "Test Result":
                                if (line.size() > 1) {
                                    result = line.get(1);
                                }
                                break;
                            case "Test Disclaimer":
                                if (line.size() > 1) {
                                    testDisclaimer = line.get(1);
                                }
                                break;
                            case "Melt Peaks":
                                line = reader.readNext();
                                if (line.size() > 0) {
                                    meltPeaks = line.get(0);
                                } else {
                                    line = reader.readNext();
                                    if (line.size() > 0) {
                                        meltPeaks = line.get(0);
                                    }
                                }
                                break;
                            case "ERROR":
                                line = reader.readNext();
                                if (line.size() > 1) {
                                    ICommonsList<String> errorHeaders = line;
                                    linesloop:
                                    while ((line = reader.readNext()) != null) {
                                        if (line.size() < errorHeaders.size()) {
                                            break;
                                        }
                                        for (int i = 0; i < errorHeaders.size(); i++) {
                                            if (errorHeaders.get(i).equals("Detail")) {
                                                error = line.get(i);
                                                break linesloop;
                                            }
                                        }
                                    }
                                } else if (line.size() == 1) {
                                    error = line.get(0);
                                }
                                break;
                            case "Messages":
                                line = reader.readNext();
                                if (line.size() > 0) {
                                    messages = line.get(0);
                                } else {
                                    line = reader.readNext();
                                    if (line.size() > 0) {
                                        messages = line.get(0);
                                    }
                                }
                                break;
                            case "History":
                            case "Run History":
                                line = reader.readNext();
                                if (line.size() > 0) {
                                    history = line.get(0);
                                } else {
                                    line = reader.readNext();
                                    if (line.size() > 0) {
                                        history = line.get(0);
                                    }
                                }
                                break;
                            case "Detail":
                                line = reader.readNext();
                                if (line.size() > 0) {
                                    detail = line.get(0);
                                } else {
                                    line = reader.readNext();
                                    if (line.size() > 0) {
                                        detail = line.get(0);
                                    }
                                }
                                break;
                            case "RESULT TABLE":
                                GeneXpertData testResult = new GeneXpertData();
                                resultsLoop:
                                while ((line = reader.readNext()) != null) { // start
                                    // loop store individual tests
                                    name = line.get(0);
                                    switch (name) {
                                        case "ASSAY INFORMATION":
                                            saveGeneXpertData(testResults, generalData);
                                            totalTests = totalTests + testResults.size();
                                            testResults = new ArrayList<>();
                                            break resultsLoop;
                                        case "RESULT TABLE":
                                            testResult = new GeneXpertData();
                                            break;
                                        case "Sample ID":
                                            if (line.size() > 1) {
                                                sampleId = line.get(1);
                                            }
                                            break;
                                        case "Patient ID":
                                            if (line.size() > 1) {
                                                patientId = " ";
                                                String text = line.get(1).replaceAll("-", "");
                                                if (StringUtils.isNumericSpace(text)) {
                                                    patientId = line.get(1);
                                                }
                                            }
                                            break;
                                        case "Test Type":
                                            if (line.size() > 1) {
                                                testType = line.get(1);
                                            }
                                            break;
                                        case "Sample Type":
                                            if (line.size() > 1) {
                                                sampleType = line.get(1);
                                            }
                                            break;
                                        case "Status":
                                            if (line.size() > 1) {
                                                status = line.get(1);
                                            }
                                            break;
                                        case "Notes":
                                            if (line.size() > 1) {
                                                notes = line.get(1);
                                            }
                                            break;
                                        case "Start Time":
                                            if (line.size() > 1) {
                                                String dateString = line.get(1);
                                                LocalDateTime fileDate = DateUtils.parseGeneXpertTime(dateString);
                                                if (fileDate != null) {
                                                    if (fileDate.isBefore(LocalDateTime.now()))
                                                        startTime = fileDate;
                                                    else
                                                        startTime = DateUtils.parseGeneXpertTime2(dateString);
                                                }
                                            }
                                            break;
                                        case "End Time":
                                            if (line.size() > 1) {
                                                String dateString = line.get(1);
                                                LocalDateTime fileDate = DateUtils.parseGeneXpertTime(dateString);
                                                if (fileDate != null) {
                                                    if (fileDate.isBefore(LocalDateTime.now()))
                                                        endTime = fileDate;
                                                    else
                                                        endTime = DateUtils.parseGeneXpertTime2(dateString);
                                                }
                                            }
                                            break;
                                        case "Error Status":
                                            if (line.size() > 1) {
                                                errorStatus = line.get(1);
                                            }
                                            break;
                                        case "Reagent Lot ID":
                                            if (line.size() > 1) {
                                                reagentLotId = line.get(1);
                                            }
                                            break;
                                        case "Expiration Date":
                                            if (line.size() > 1) {
                                                String dateString = line.get(1);
                                                LocalDate fileDate = DateUtils.parseGeneXpertShort(dateString);
                                                if (fileDate != null) {
                                                    if (fileDate.isBefore(LocalDate.now()))
                                                        expirationDate = fileDate;
                                                    else
                                                        expirationDate = DateUtils.parseGeneXpertShort2(dateString);
                                                }
                                            }
                                            break;
                                        case "Cartridge Index":
                                            if (line.size() > 1) {
                                                cartridgeIndex = line.get(1);
                                            }
                                            break;
                                        case "Cartridge S/N":
                                            if (line.size() > 1) {
                                                cartridgeSN = line.get(1);
                                            }
                                            break;
                                        case "Module Name":
                                            if (line.size() > 1) {
                                                moduleName = line.get(1);
                                            }
                                            break;
                                        case "Module S/N":
                                            if (line.size() > 1) {
                                                moduleSN = line.get(1);
                                            }
                                            break;
                                        case "Instrument S/N":
                                            if (line.size() > 1) {
                                                instrumentSN = line.get(1);
                                            }
                                            break;
                                        case "S/W Version":
                                            if (line.size() > 1) {
                                                softwareVersion = line.get(1);
                                            }
                                            break;
                                        case "Test Result":
                                            if (line.size() > 1) {
                                                result = line.get(1);
                                            }
                                            break;
                                        case "Test Disclaimer":
                                            if (line.size() > 1) {
                                                testDisclaimer = line.get(1);
                                            }
                                            break;
                                        case "Melt Peaks":
                                            line = reader.readNext();
                                            if (line.size() > 0) {
                                                meltPeaks = line.get(0);
                                            } else {
                                                line = reader.readNext();
                                                if (line.size() > 0) {
                                                    meltPeaks = line.get(0);
                                                }
                                            }
                                            break;
                                        case "ERROR":
                                            line = reader.readNext();
                                            if (line.size() > 1) {
                                                ICommonsList<String> errorHeaders = line;
                                                linesloop:
                                                while ((line = reader.readNext()) != null) {
                                                    if (line.size() < errorHeaders.size()) {
                                                        break;
                                                    }
                                                    for (int i = 0; i < errorHeaders.size(); i++) {
                                                        if (errorHeaders.get(i).equals("Detail")) {
                                                            error = line.get(i);
                                                            break linesloop;
                                                        }
                                                    }
                                                }
                                            } else if (line.size() == 1) {
                                                error = line.get(0);
                                            }
                                            break;
                                        case "Messages":
                                            line = reader.readNext();
                                            if (line.size() > 0) {
                                                messages = line.get(0);
                                            } else {
                                                line = reader.readNext();
                                                if (line.size() > 0) {
                                                    messages = line.get(0);
                                                }
                                            }
                                            break;
                                        case "History":
                                            line = reader.readNext();
                                            if (line.size() > 0) {
                                                history = line.get(0);
                                            } else {
                                                line = reader.readNext();
                                                if (line.size() > 0) {
                                                    history = line.get(0);
                                                }
                                            }
                                            break;
                                        case "Run History":
                                            line = reader.readNext();
                                            if (line.size() > 0) {
                                                history = line.get(0);
                                            } else {
                                                line = reader.readNext();
                                                if (line.size() > 0) {
                                                    history = line.get(0);
                                                }
                                            }
                                            if (!testResults.contains(testResult)) {
                                                testResult.setStartTime(startTime);
                                                testResult.setEndTime(endTime);
                                                testResult.setSampleId(sampleId);
                                                testResult.setPatientId(patientId);
                                                testResult.setTestType(testType);
                                                testResult.setSampleType(sampleType);
                                                testResult.setStatus(errorStatus);
                                                testResult.setNotes(notes);
                                                testResult.setNotes(notes);
                                                testResult.setErrorStatus(errorStatus);
                                                testResult.setReagentLotId(reagentLotId);
                                                testResult.setExpirationDate(expirationDate);
                                                testResult.setCartridgeIndex(cartridgeIndex);
                                                testResult.setCartridgeSN(cartridgeSN);
                                                testResult.setModuleName(moduleName);
                                                testResult.setModuleSN(moduleSN);
                                                testResult.setInstrumentSN(instrumentSN);
                                                testResult.setSoftwareVersion(softwareVersion);
                                                testResult.setTestResult(result);
                                                testResult.setTestDisclaimer(testDisclaimer);
                                                testResult.setMeltPeaks(meltPeaks);
                                                testResult.setError(error);
                                                testResult.setMessages(messages);
                                                testResult.setHistory(history);
                                                testResult.setDetail(detail);
                                                testResult.setStatus(status);
                                                testResults.add(testResult);
                                                testResult = new GeneXpertData();
                                            }
                                            break;
                                        case "Detail":
                                            line = reader.readNext();
                                            if (line.size() > 0) {
                                                detail = line.get(0);
                                            } else {
                                                line = reader.readNext();
                                                if (line.size() > 0) {
                                                    testResult.setDetail(line.get(0));
                                                }
                                            }
                                            break;
                                        case "Analyte Result":
                                            line = reader.readNext();
                                            if (line.size() == 1) {
                                                line = reader.readNext();
                                            }
                                            if (line.size() > 0) {
                                                ICommonsList<String> analyteHeaders = line;
                                                List<GenexpertTestAnalyte> testAnalyteResults = new ArrayList<>();
                                                while ((line = reader.readNext()) != null) {
                                                    if ((!line.get(0).equals("")) && (line.size() < analyteHeaders.size())) {
                                                        break;
                                                    }
                                                    if (line.size() != 1) {
                                                        GenexpertTestAnalyte analyteResult = new GenexpertTestAnalyte();
                                                        for (int i = 0; i < analyteHeaders.size(); i++) {
                                                            String analyteHeaderName = analyteHeaders.get(i);
                                                            switch (analyteHeaderName) {
                                                                case "Analyte Name":
                                                                    analyteResult.setAnalyteName(line.get(i));
                                                                    break;
                                                                case "Ct":
                                                                    analyteResult.setCt(line.get(i));
                                                                    break;
                                                                case "EndPt":
                                                                    analyteResult.setEndPt(line.get(i));
                                                                    break;
                                                                case "Analyte Result":
                                                                    analyteResult.setAnalyteResult(line.get(i));
                                                                    break;
                                                                default:
                                                                    break;
                                                            }
                                                        }
                                                        analyteResult.setDeviceName(generalData.getDeviceName());
                                                        testAnalyteResults.add(analyteResult);
                                                    }
                                                }
                                                testResult.setTestAnalyteList(testAnalyteResults);
                                            }
                                            break;
                                        case "Test and Analyte Result":
                                            line = reader.readNext();
                                            if (line.size() > 0) {
                                                ICommonsList<String> analyteHeaders = line;
                                                List<GenexpertTestAnalyte> testAnalyteResults = new ArrayList<>();
                                                while ((line = reader.readNext()) != null) {
                                                    if (line.size() < analyteHeaders.size()) {
                                                        break;
                                                    }
                                                    GenexpertTestAnalyte analyteResult = new GenexpertTestAnalyte();
                                                    for (int i = 0; i < analyteHeaders.size(); i++) {
                                                        String analyteHeaderName = analyteHeaders.get(i);
                                                        switch (analyteHeaderName) {
                                                            case "Analyte Name":
                                                                analyteResult.setAnalyteName(line.get(i));
                                                                break;
                                                            case "Ct":
                                                                analyteResult.setCt(line.get(i));
                                                                break;
                                                            case "EndPt":
                                                                analyteResult.setEndPt(line.get(i));
                                                                break;
                                                            case "Analyte Result":
                                                                analyteResult.setAnalyteResult(line.get(i));
                                                                break;
                                                            case "Probe Check Result":
                                                                analyteResult.setProbeCheckResult(line.get(i));
                                                                break;
                                                            default:
                                                                break;
                                                        }
                                                    }
                                                    analyteResult.setDeviceName(generalData.getDeviceName());
                                                    testAnalyteResults.add(analyteResult);
                                                }
                                                testResult.setTestAnalyteList(testAnalyteResults);
                                            }
                                            break;
                                        case "Probe Check Details":
                                            line = reader.readNext();
                                            if (line.size() > 0) {
                                                ICommonsList<String> probeCheckHeaders = line;
                                                List<GenexpertProbecheckDetail> probeCheckResults = new ArrayList<>();
                                                while ((line = reader.readNext()) != null) {
                                                    if (line.size() < probeCheckHeaders.size()) {
                                                        break;
                                                    }
                                                    GenexpertProbecheckDetail probeCheckResult = new GenexpertProbecheckDetail();
                                                    for (int i = 0; i < probeCheckHeaders.size(); i++) {
                                                        String probeCheckHeaderName = probeCheckHeaders.get(i);
                                                        switch (probeCheckHeaderName) {
                                                            case "Analyte Name":
                                                                probeCheckResult.setAnalyteName(line.get(i));
                                                                break;
                                                            case "Prb Chk 1":
                                                                probeCheckResult.setPrbChk1(line.get(i));
                                                                break;
                                                            case "Prb Chk 2":
                                                                probeCheckResult.setPrbChk2(line.get(i));
                                                                break;
                                                            case "Prb Chk 3":
                                                                probeCheckResult.setPrbChk3(line.get(i));
                                                                break;
                                                            case "Probe Check Result":
                                                                probeCheckResult.setProbeCheckResult(line.get(i));
                                                                break;
                                                            default:
                                                                break;
                                                        }
                                                    }
                                                    probeCheckResult.setDeviceName(generalData.getDeviceName());
                                                    probeCheckResults.add(probeCheckResult);
                                                }
                                                testResult.setProbecheckDetailList(probeCheckResults);
                                            }
                                            break;
                                        default:
                                            break;

                                    }

                                } // End of individual tests loop

                                if (testResult.getSampleId() != null) {
                                    if (!testResults.contains(testResult)) {
                                        testResult.setStartTime(startTime);
                                        testResult.setEndTime(endTime);
                                        testResult.setTestType(testType);
                                        testResult.setSampleType(sampleType);
                                        testResult.setStatus(errorStatus);
                                        testResult.setNotes(notes);
                                        testResult.setNotes(notes);
                                        testResult.setErrorStatus(errorStatus);
                                        testResult.setReagentLotId(reagentLotId);
                                        testResult.setExpirationDate(expirationDate);
                                        testResult.setCartridgeIndex(cartridgeIndex);
                                        testResult.setCartridgeSN(cartridgeSN);
                                        testResult.setModuleName(moduleName);
                                        testResult.setModuleSN(moduleSN);
                                        testResult.setInstrumentSN(instrumentSN);
                                        testResult.setSoftwareVersion(softwareVersion);
                                        testResult.setTestResult(result);
                                        testResult.setTestDisclaimer(testDisclaimer);
                                        testResult.setMeltPeaks(meltPeaks);
                                        testResult.setError(error);
                                        testResult.setMessages(messages);
                                        testResult.setHistory(history);
                                        testResult.setDetail(detail);
                                        testResult.setStatus(status);
                                        testResults.add(testResult);
                                        testResult = new GeneXpertData();
                                    }
                                }
                                break;
                            // break generalDataLoop;
                            default:
                                break;
                        }
                    } // End loop for general data
                    Optional<Device> optionalDevice = deviceRepository.getDeviceBySerialNumber(generalData.getDeviceName());
                    if (optionalDevice.isEmpty()) {
                        device = new Device();
                        device.setSerialNumber(generalData.getDeviceName());
                        device.setDeviceType(DeviceType.CEPHEID_GENEXPERT);
                        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
                        if (nodeConfiguration.isPresent())
                            device.setTestingPoint(nodeConfiguration.get().getFacility());
                        device = deviceRepository.save(device);
                        refresh = true;
                        log.info("New genexpert device " + device.getSerialNumber() + " registered");
                    } else {
                        device = optionalDevice.get();
                    }

                    List<Genexpert> insertedRows = saveGeneXpertData(testResults, generalData);
                    insertedTests = insertedTests + insertedRows.size();
                    totalTests = totalTests + testResults.size();
                    MessageNotification messageNotification = messageNotificationRepository.findByTriggerName(NotificationMessage.DATA_SENT_TO_NODE.getTriggerName());
                    if (messageNotification.getStatus()) {
                        String message = messageNotification.getText();
                        Broadcaster.broadcast(message.replace("#deviceCode", device.getSerialNumber()), NotificationVariant.LUMO_SUCCESS);
                    }
                    for (Genexpert element : insertedRows) {
                        deviceService.updateCartridgeByLotId(element.getReagentLotId(), device);
                    }
                    if (insertedRows.size() > 0) {
                        log.info("Genexpert file " + file.getName() + "imported");
                        this.genexpertAPIService.postTests();
                    }
                } else {
                    log.severe(file.getName() + " is not valid GeneXpert file");
                }
                FileUtils.deleteQuietly(file);
            }
            if (insertedTests > 0 && device != null) {
                deviceService.verifyRemainingInventoryByDevice(device);
                deviceService.sendCartridgeInventory();
            }
            if (refresh) {
                Broadcaster.broadcast("reload", null);
                Broadcaster.broadcast("Device " + device.getDeviceType().getScreenName() + " registered in the node",
                        NotificationVariant.LUMO_SUCCESS);
            }
        }
        answer.put("files", totalFiles);
        answer.put("tests", totalTests);
        return answer;
    }

    private List<Genexpert> saveGeneXpertData(List<GeneXpertData> testResults, GeneXpertGeneralData generalData) {
        List<Genexpert> genexpertList = new ArrayList<>();
        for (GeneXpertData data : testResults) {
            Genexpert genexpertResult = new Genexpert();
            genexpertResult.setAssay(generalData.getAssay());
            genexpertResult.setAssayDisclaimer(generalData.getAssayDisclaimer());
            genexpertResult.setAssayType(generalData.getAssayType());
            genexpertResult.setAssayVersion(generalData.getAssayVersion());
            genexpertResult.setDetail(data.getDetail());
            genexpertResult.setCartridgeIndex(data.getCartridgeIndex());
            genexpertResult.setDeviceName(generalData.getDeviceName());
            genexpertResult.setEndTime(data.getEndTime());
            genexpertResult.setError(data.getError());
            genexpertResult.setExportedDate(generalData.getExportedDate());
            genexpertResult.setCartridgeIndex(data.getCartridgeIndex());
            genexpertResult.setSpecificParameters(generalData.getSpecificParameters());
            genexpertResult.setUser(generalData.getUser());
            genexpertResult.setTestType(data.getTestType());
            genexpertResult.setTestDisclaimer(data.getTestDisclaimer());
            genexpertResult.setReagentLotNumber(generalData.getReagentLotNumber());
            genexpertResult.setCartridgeSn(data.getCartridgeSN());
            genexpertResult.setErrorStatus(data.getErrorStatus());
            genexpertResult.setExpirationDate(data.getExpirationDate());
            genexpertResult.setHistory(data.getHistory());
            genexpertResult.setInstrumentSn(data.getInstrumentSN());
            genexpertResult.setMeltPeaks(data.getMeltPeaks());
            genexpertResult.setMessages(data.getMessages());
            genexpertResult.setModuleName(data.getModuleName());
            genexpertResult.setModuleSn(data.getModuleSN());
            genexpertResult.setNotes(data.getNotes());
            genexpertResult.setPatientId(data.getPatientId());
            genexpertResult.setReagentLotId(data.getReagentLotId());
            genexpertResult.setSampleId(data.getSampleId());
            genexpertResult.setSampleType(data.getSampleType());
            genexpertResult.setSoftwareVersion(data.getSoftwareVersion());
            genexpertResult.setStartTime(data.getStartTime());
            genexpertResult.setStatus(data.getStatus());
            genexpertResult.setTestDisclaimer(data.getTestDisclaimer());
            genexpertResult.setTestResult(data.getTestResult());
            genexpertResult.setTestType(data.getTestType());
            try {
                genexpertResult = genexpertRepository.save(genexpertResult);
                if (data.getTestAnalyteList() != null)
                    for (GenexpertTestAnalyte testAnalyte : data.getTestAnalyteList()) {
                        testAnalyte.setTestId(genexpertResult.getId());
                        genexpertTestAnalyteRepository.save(testAnalyte);
                    }
                if (data.getProbecheckDetailList() != null)
                    for (GenexpertProbecheckDetail probecheckDetail : data.getProbecheckDetailList()) {
                        probecheckDetail.setTestId(genexpertResult.getId());
                        genexpertProbecheckDetailRepository.save(probecheckDetail);
                    }
                genexpertList.add(genexpertResult);
            } catch (DataIntegrityViolationException e) {
                log.warning(e.getMessage());
            }
        }
        return genexpertList;
    }

    @Override
    public void exportGeneXpertDataToFile(File targetFolder) throws IOException {
        Optional<NodeHealthStatistics> nodeHealthStatistics = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();
        String macId = "";
        if(nodeHealthStatistics.isPresent())
            macId = nodeHealthStatistics.get().getMacId();

        List<Genexpert> list = genexpertRepository.findAllByIsExportedFalse();
        if (list.isEmpty()) {
            Optional<FtpGenexpertInfo> ftpGenexpertInfo = ftpGenexpertInfoRepository.getTopByOrderByUploadTimeDesc();
            if (ftpGenexpertInfo.isPresent() && !ftpGenexpertInfo.get().getIsExported()) {
                this.writeGeneXpertSummaryFile(ftpGenexpertInfo.get(), macId);
            }
            return;
        }

        String NEW_LINE_SEPARATOR = "\n";
        // Create the CSVFormat object with "," as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();

        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty() || !nodeConfiguration.get().getIsExported()) {
            log.warning("Node configuration isn't exported yet. Genexpert Data won't get exported.");
            return;
        }
        String fName = DeviceType.CEPHEID_GENEXPERT + "-" + nodeConfiguration.get().getFacility();
        String fUniqueKey = new SimpleDateFormat("yyMMddhhmmssSSS").format(new Date());
        File expFile = new File(targetFolder + File.separator + fName + "-"
                + list.get(0).getDeviceName() + "(" + fUniqueKey + ").csv");
        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        // initialize CSVPrinter object
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
        // write header
        csvFilePrinter.printRecord(Constants.geneXpertTemplateFormat_H);

        for (Genexpert data : list) {
            List<Object> tempData = this.writeGeneXpertResults(data, macId, nodeConfiguration.get());
            csvFilePrinter.printRecord(tempData);
            exportGeneXpertProbeCheckResultsToFile(targetFolder, data.getId());
            exportGeneXpertTestAnalyteResultsToFile(targetFolder, data.getId());
        }
        for (Genexpert test : list) {
            test.setIsExported(true);
            genexpertRepository.save(test);
        }
        log.info(DeviceType.CEPHEID_GENEXPERT.getScreenName() + " file was created successfully");
    }

    @Override
    public void pushGeneXpertFileToServer(File sourceFolder) throws JSchException {
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty())
            return;
        String country = nodeConfiguration.get().getCountry();
        String remoteServerIPAddress = "";
        if (country.equals("Ethiopia"))
            remoteServerIPAddress = propertiesService.getPropertyValue("ethiopia.remote.server.ipaddress");
        else
            remoteServerIPAddress = propertiesService.getPropertyValue("aws.remote.server.ipaddress");
        String remoteServerCertificate = propertiesService.getFullPropertyValue("remote.server.certificate.path");
        String remoteDirectory = propertiesService.getPropertyValue("remote.server.genexpert.directory");
        String remoteServerUser = propertiesService.getPropertyValue("remote.server.user");
        String remoteServerPort = propertiesService.getPropertyValue("remote.server.port");

        if (sourceFolder.listFiles().length > 0) {
            JSch jsch = new JSch();
            String user = remoteServerUser;
            String host = remoteServerIPAddress;
            int port = Integer.parseInt(remoteServerPort);
            jsch.addIdentity(remoteServerCertificate);
            @Cleanup("disconnect") Session session = jsch.getSession(user, host, port);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            @Cleanup("disconnect") Channel channel = session.openChannel("sftp");
            channel.connect(60000);
            @Cleanup("disconnect") ChannelSftp sftp = (ChannelSftp) channel;
            try {
                sftp.cd(remoteDirectory);
                for (final File fileEntry : sourceFolder.listFiles()) {
                    if (fileEntry.getName().endsWith(".csv")) {
                        sftp.put(fileEntry.getPath(), fileEntry.getName());
                        FileUtils.deleteQuietly(fileEntry);
                    }
                }
            } catch (SftpException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private void writeGeneXpertSummaryFile(FtpGenexpertInfo ftpInfo, String macId) throws FileNotFoundException, UnsupportedEncodingException {
        String exportPath = propertiesService.getFullPropertyValue("ftp.file.destination.folder");
        Integer exportRecordLimit = Integer
                .parseInt(propertiesService.getPropertyValue("bd.presto.export.record.limit"));
        Integer totalFiles = (int) Math.ceil((double) ftpInfo.getTotalTests() / exportRecordLimit);

        DateTimeFormatter fileNameFormat = DateTimeFormatter.ofPattern("MMddyyHHmmss");
        @Cleanup PrintWriter writer = new PrintWriter(
                exportPath + File.separator + "genexpertsummary" +
                        ftpInfo.getUploadTime().format(fileNameFormat) + ".txt", "UTF-8");
        writer.println("macAddress: " + macId);
        writer.println("TotalFiles: " + totalFiles);
        writer.println("totalTests: " + ftpInfo.getTotalTests());
        DateTimeFormatter f = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss");
        writer.println("importDate: " + ftpInfo.getUploadTime().format(f));
        writer.println("------------------------------------");
        ftpInfo.setIsExported(true);
        ftpGenexpertInfoRepository.save(ftpInfo);
    }

    private List<Object> writeGeneXpertResults(Genexpert genexpert, String macId, NodeConfiguration nodeConfiguration) {
        List<Object> tempData = new ArrayList<>();
        try {
            tempData.add(macId);
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(nodeConfiguration.getCountry());
            tempData.add(nodeConfiguration.getProvince());
            tempData.add(nodeConfiguration.getDistrict());
            tempData.add(nodeConfiguration.getFacility());
            tempData.add(genexpert.getDeviceName());
            tempData.add(genexpert.getAssay());
            tempData.add(genexpert.getAssayDisclaimer());
            tempData.add(genexpert.getAssayType());
            tempData.add(genexpert.getAssayVersion());
            tempData.add(genexpert.getExportedDate().format(DateUtils.TIME_FORMAT));
            tempData.add(genexpert.getSpecificParameters());
            tempData.add(genexpert.getReagentLotNumber());
            tempData.add(genexpert.getUser());
            tempData.add(genexpert.getCartridgeSn());
            tempData.add(genexpert.getEndTime().format(DateUtils.TIME_FORMAT));
            tempData.add(genexpert.getError());
            tempData.add(genexpert.getErrorStatus());
            tempData.add(genexpert.getExpirationDate().format(DateUtils.DATE_FORMAT));
            tempData.add(genexpert.getHistory());
            tempData.add(genexpert.getInstrumentSn());
            tempData.add(genexpert.getMeltPeaks());
            tempData.add(genexpert.getMessages());
            tempData.add(genexpert.getModuleName());
            tempData.add(genexpert.getModuleSn());
            tempData.add(genexpert.getNotes());
            tempData.add(genexpert.getPatientId());
            tempData.add(genexpert.getReagentLotId());
            tempData.add(genexpert.getSampleId());
            tempData.add(genexpert.getSampleType());
            tempData.add(genexpert.getSoftwareVersion());
            tempData.add(genexpert.getStartTime().format(DateUtils.TIME_FORMAT));
            tempData.add(genexpert.getStatus());
            tempData.add(genexpert.getTestDisclaimer());
            tempData.add(genexpert.getTestResult());
            tempData.add(genexpert.getTestType());
            tempData.add(genexpert.getId());
            tempData.add(genexpert.getSysLogDate().format(DateUtils.TIME_FORMAT));
            if (genexpert.getNascopTimestamp() != null)
                tempData.add(genexpert.getNascopTimestamp().format(DateUtils.TIME_FORMAT));

            return tempData;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in CsvFileWriter for --" + genexpert.getDeviceName()
                    + "-- operation failed", e);
            return tempData;
        }

    }


    private void exportGeneXpertProbeCheckResultsToFile(File geneXpertExportPath, Long testId) throws IOException {
        String NEW_LINE_SEPARATOR = "\n";
        // Create the CSVFormat object with "," as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty())
            return;
        if (!nodeConfiguration.get().getIsExported())
            return;

        List<GenexpertProbecheckDetail> list = genexpertProbecheckDetailRepository.findAllByTestId(testId);
        if (list.isEmpty())
            return;
        String fName = "GenExpertProbeCheckResults" + "-" + nodeConfiguration.get().getFacility();
        String fUniqueKey = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date());
        File expFile = new File(
                geneXpertExportPath + File.separator + fName + "-" + testId + "(" + fUniqueKey + ").csv");

        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

        // write header
        csvFilePrinter.printRecord(Constants.geneXpertProbeCheckTemplateFormat_H);
        for (GenexpertProbecheckDetail data : list) {
            List<Object> tempData = new ArrayList<>();
            tempData = this.writeGeneXpertProbeCheckResults(data);
            csvFilePrinter.printRecord(tempData);
        }
        log.info(DeviceType.CEPHEID_GENEXPERT + " probe results file was created successfully");
        for (GenexpertProbecheckDetail detail : list) {
            detail.setIsExported(true);
            genexpertProbecheckDetailRepository.save(detail);
        }
    }

    private void exportGeneXpertTestAnalyteResultsToFile(File geneXpertExportPath, Long testId) throws IOException {
        String NEW_LINE_SEPARATOR = "\n";
        // Create the CSVFormat object with "," as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(NEW_LINE_SEPARATOR).build();
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isEmpty())
            return;
        if (!nodeConfiguration.get().getIsExported())
            return;

        List<GenexpertTestAnalyte> list = genexpertTestAnalyteRepository.findAllByTestId(testId);
        if (list.isEmpty())
            return;
        String fName = "GenExpertTestAnalyteResults" + "-" + nodeConfiguration.get().getFacility();
        String fUniqueKey = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date());
        File expFile = new File(
                geneXpertExportPath + File.separator + fName + "-" + testId + "(" + fUniqueKey + ").csv");

        @Cleanup FileWriter fileWriter = new FileWriter(expFile);
        @Cleanup CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

        csvFilePrinter.printRecord(Constants.geneXpertTestAnalyteTemplateFormat_H);

        for (GenexpertTestAnalyte data : list) {
            List<Object> tempData = new ArrayList<>();
            tempData = writeGeneXpertTestAnalyteResults(data);
            csvFilePrinter.printRecord(tempData);
        }
        log.info(DeviceType.CEPHEID_GENEXPERT + " test analyte file was created successfully");
        for (GenexpertTestAnalyte detail : list) {
            detail.setIsExported(true);
            genexpertTestAnalyteRepository.save(detail);
        }
    }

    private List<Object> writeGeneXpertTestAnalyteResults(GenexpertTestAnalyte data) {
        List<Object> tempData = new ArrayList<>();
        tempData.add(data.getTestId());
        tempData.add(data.getAnalyteName());
        tempData.add(data.getCt());
        tempData.add(data.getEndPt());
        tempData.add(data.getAnalyteResult());
        tempData.add(data.getProbeCheckResult());
        tempData.add(data.getDeviceName());
        return tempData;
    }

    private List<Object> writeGeneXpertProbeCheckResults(GenexpertProbecheckDetail data) {
        List<Object> tempData = new ArrayList<>();
        tempData.add(data.getTestId());
        tempData.add(data.getAnalyteName());
        tempData.add(data.getPrbChk1());
        tempData.add(data.getPrbChk2());
        tempData.add(data.getPrbChk3());
        tempData.add(data.getProbeCheckResult());
        tempData.add(data.getDeviceName());
        return tempData;
    }


}
