/**
 * CD4PrestoAPIServiceImpl.java Created: Mar 22, 2018 JavierAngelH
 */

package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.dto.ChaiFacsprestoSummaryDTO;
import com.edgedx.connectpoc.dto.ChaiPatientSampleDTO;
import com.edgedx.connectpoc.entity.FacsprestoPatientSample;
import com.edgedx.connectpoc.entity.NodeConfiguration;
import com.edgedx.connectpoc.repository.CustomRepository;
import com.edgedx.connectpoc.repository.FacsprestoPatientSampleRepository;
import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Log
public class PatientSampleFacsprestoAPIServiceImpl implements PatientSampleFacsprestoAPIService {

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    FacsprestoPatientSampleRepository facsprestoPatientSampleRepository;

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Autowired
    CustomRepository customRepository;

    @Override
    public void postIndividualTests() {
        String country = "";
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isPresent()) {
            country = nodeConfiguration.get().getCountry();
        } else {
            log.severe("Node not configured. Can't post genexpert tests to API");
            return;
        }
        if (country.trim().equalsIgnoreCase("KENYA")) {
            log.info("starting to send individual PatientSample tests to server ");
            List<ChaiPatientSampleDTO> list = customRepository.getChaiFacsPrestoNotSentTests();
            RestTemplate restTemplate = new RestTemplate();
            String url = propertiesService.getPropertyValue("api.patientsample.individual.url");
            Map<String, String> map = new HashMap<>();
            for (ChaiPatientSampleDTO patientSampleObject : list) {
                String answer = restTemplate.postForObject(url, patientSampleObject, String.class, map);
                FacsprestoPatientSample sample = facsprestoPatientSampleRepository.findById(patientSampleObject.getId()).get();
                if (answer.equals("true")) {
                    sample.setApiExported(true);
                    sample.setNascopTimestamp(LocalDateTime.now());
                    facsprestoPatientSampleRepository.save(sample);
                    log.info("Individual Test " + patientSampleObject.getRunId() + "  succesfully sent to the server");
                } else {
                    if (answer.contains("already exists in database")) {
                        sample.setApiExported(true);
                        sample.setNascopTimestamp(LocalDateTime.now());
                        facsprestoPatientSampleRepository.save(sample);
                    }
                    log.warning("Failed to transmit Individual Test  to the server. " + answer);
                }
            }
        }
    }

    @Override
    public void postMonthlySummaryByDevice() {
        log.info("starting to send monthly Summary to server ");
        String country = "";
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isPresent()) {
            country = nodeConfiguration.get().getCountry();
        } else {
            log.severe("Node not configured. Can't post genexpert tests to API");
            return;
        }
        if (country.trim().equalsIgnoreCase("KENYA")) {
            List<ChaiFacsprestoSummaryDTO> list = customRepository.getChaiSummaryDeviceDate(LocalDate.now().getMonthValue(), LocalDate.now().getYear());
            if (!list.isEmpty()) {
                RestTemplate restTemplate = new RestTemplate();
                String url = propertiesService.getPropertyValue("api.patientsample.monthlysummary.url");
                Map<String, String> map = new HashMap<>();

                for (ChaiFacsprestoSummaryDTO object : list) {
                    String answer = restTemplate.postForObject(url, object, String.class, map);
                    if (answer.equals("true")) {
                        log.info("monthly summary succesfully sent to the server");
                    } else {
                      log.warning("Failed to transmit monthly summary to the server. " + answer);
                    }
                }
            }
        }
    }

    @Override
    public String postMonthSummaryByDevice(int month, int year) {
        String message = "";
        List<ChaiFacsprestoSummaryDTO> list = customRepository.getChaiSummaryDeviceDate(month, year);
        if (!list.isEmpty()) {
            RestTemplate restTemplate = new RestTemplate();
            String url = propertiesService.getPropertyValue("api.patientsample.monthlysummary.url");
            Map<String, String> map = new HashMap<>();

            for (ChaiFacsprestoSummaryDTO object : list) {
                String answer = restTemplate.postForObject(url, object, String.class, map);
                if (answer.equals("true")) {
                    message = "Monthly summary successfully sent to the server";
                } else {
                    message = "Failed to transmit monthly summary to the server. " + answer;
                }
            }
        } else message = "There is no test data for this month";

        return message;
    }


}
