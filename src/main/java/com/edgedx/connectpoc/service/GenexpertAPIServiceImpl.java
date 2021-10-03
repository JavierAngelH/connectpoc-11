package com.edgedx.connectpoc.service;


import com.edgedx.connectpoc.dto.EthiopiaCovidDTO;
import com.edgedx.connectpoc.dto.GenexpertChaiAPIResponseDTO;
import com.edgedx.connectpoc.dto.GenexpertChaiDTO;
import com.edgedx.connectpoc.entity.Genexpert;
import com.edgedx.connectpoc.entity.NodeConfiguration;
import com.edgedx.connectpoc.repository.CustomRepository;
import com.edgedx.connectpoc.repository.GenexpertRepository;
import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import com.edgedx.connectpoc.utils.LoggingInterceptor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;

@Service
@Log
public class GenexpertAPIServiceImpl implements GenexpertAPIService {

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Autowired
    CustomRepository customRepository;

    @Autowired
    GenexpertRepository genexpertRepository;

    @Override
    public void postTests() {
        String country = "";
        String facility = "";
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isPresent()) {
            country = nodeConfiguration.get().getCountry();
            facility = nodeConfiguration.get().getFacility();
        } else {
            log.severe("Node not configured. Can't post genexpert tests to API");
            return;
        }

        switch (country.trim().toUpperCase(Locale.ROOT)) {
            case "KENYA":
                List<GenexpertChaiDTO> chaiList = customRepository.getChaiHIVNotSentTests();
                if (!chaiList.isEmpty())
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
                        if (interceptors.isEmpty()) {
                            interceptors = new ArrayList<>();
                        }
                        interceptors.add(new LoggingInterceptor());
                        restTemplate.setInterceptors(interceptors);
                        String url = this.propertiesService.getPropertyValue("api.nascop.genexpert.url");
                        Map<String, String> map = new HashMap<>();
                        for (GenexpertChaiDTO testObject : chaiList) {
                            HttpHeaders headers = new HttpHeaders();
                            HttpEntity<GenexpertChaiDTO> request = new HttpEntity<>(testObject, headers);
                            ResponseEntity<GenexpertChaiAPIResponseDTO> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
                                    request, GenexpertChaiAPIResponseDTO.class);
                            if (responseEntity.getBody().getStatus().equals("ok")) {
                                Genexpert genexpert = genexpertRepository.findById(testObject.getId()).get();
                                genexpert.setApiExported(true);
                                genexpert.setNascopTimestamp(LocalDateTime.now());
                                genexpertRepository.save(genexpert);
                                log.info("Test " + testObject.getSampleId() + "  succesfully sent to the server");
                            } else {
                                log.warning("Failed: " + testObject.getSampleId() + " " + responseEntity.getBody().getStatus());
                            }
                        }
                    } catch (Exception e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                    }
                break;
            case "ETHIOPIA":
                try {
                    List<EthiopiaCovidDTO> ethiopiaList = customRepository.getEthiopiaGenexpertCovidNotSentData();
                    RestTemplate restTemplate = new RestTemplate();
                    List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
                    if (interceptors.isEmpty()) {
                        interceptors = new ArrayList<>();
                    }
                    interceptors.add(new LoggingInterceptor());
                    restTemplate.setInterceptors(interceptors);
                    String url = this.propertiesService.getPropertyValue("api.covid.ethiopia.test.url") + "/" + facility;
                    URI uri;
                    uri = new URI(url);
                    try {
                        ResponseEntity<EthiopiaCovidDTO[]> responseEntity = restTemplate.getForEntity(uri, EthiopiaCovidDTO[].class);
                        EthiopiaCovidDTO[] serverTests = responseEntity.getBody();
                        for (EthiopiaCovidDTO serverTest : serverTests) {
                            for (EthiopiaCovidDTO localResult : ethiopiaList) {
                                if (localResult.getSpecimenId().equals(serverTest.getSpecimenId()) || localResult.getSpecimenId().equals(serverTest.getRequestId())) {
                                    {
                                        serverTest.setResult(localResult.getResult());
                                        serverTest.setId(localResult.getId());
                                    }
                                }
                            }
                        }
                        List<EthiopiaCovidDTO> reportList = new ArrayList<>();
                        for (EthiopiaCovidDTO serverTest : serverTests) {
                            if (serverTest.getResult() != null) {
                                reportList.add(serverTest);
                            }
                        }
                        url = this.propertiesService.getPropertyValue("api.covid.ethiopia.test.url");
                        HttpHeaders headers = new HttpHeaders();
                        HttpEntity<EthiopiaCovidDTO[]> request = new HttpEntity<>(reportList.toArray(new EthiopiaCovidDTO[reportList.size()]), headers);
                        ResponseEntity<EthiopiaCovidDTO[]> responseEntity2 = restTemplate.exchange(url, HttpMethod.POST,
                                request, EthiopiaCovidDTO[].class);
                        EthiopiaCovidDTO[] response = responseEntity2.getBody();
                        for (EthiopiaCovidDTO dto : response) {
                            for (EthiopiaCovidDTO localDto : reportList) {
                                if (dto.getSpecimenId().equals(localDto.getSpecimenId())) {
                                    dto.setId(localDto.getId());
                                    break;
                                }
                            }
                            if (dto.getMsg().equals("OK")) {
                                Genexpert genexpert = genexpertRepository.findById(dto.getId()).get();
                                genexpert.setApiExported(true);
                                genexpertRepository.save(genexpert);
                                log.info("Test " + dto.getId() + " successfully sent to the server");
                            } else {
                                if (dto.getMsg().equals("Result already exists")){
                                    Genexpert genexpert = genexpertRepository.findById(dto.getId()).get();
                                    genexpert.setApiExported(true);
                                    genexpertRepository.save(genexpert);
                                }
                                log.warning("Failed: " + dto.getMsg() + ": " + dto.getId());
                            }
                        }
                    } catch (HttpStatusCodeException | HttpMessageNotReadableException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
                break;
            default:
                break;
        }
    }

}
