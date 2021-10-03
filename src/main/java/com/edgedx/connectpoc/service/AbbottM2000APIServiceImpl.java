package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.dto.AbbottChaiDTO;
import com.edgedx.connectpoc.dto.AbbottChaiResponseDTO;
import com.edgedx.connectpoc.dto.EthiopiaCovidDTO;
import com.edgedx.connectpoc.entity.AbbottM2000;
import com.edgedx.connectpoc.entity.Genexpert;
import com.edgedx.connectpoc.entity.NodeConfiguration;
import com.edgedx.connectpoc.repository.AbbottM2000Repository;
import com.edgedx.connectpoc.repository.CustomRepository;
import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import com.edgedx.connectpoc.utils.LoggingInterceptor;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import elemental.json.JsonObject;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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
public class AbbottM2000APIServiceImpl implements AbbottM2000APIService {

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    CustomRepository customRepository;

    @Autowired
    AbbottM2000Repository abbottM2000Repository;

    @Override
    public void postTests() {
        String country = "";
        String facility = "";

        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isPresent()) {
            country = nodeConfiguration.get().getCountry();
            facility = nodeConfiguration.get().getFacility();
        } else {
            log.severe("Node not configured. Can't post abbott tests to API");
            return;
        }

        switch (country.trim().toUpperCase(Locale.ROOT)) {
            case "KENYA":
                try {
                    List<AbbottChaiDTO> list = customRepository.getChaiAbbottNotSentTests();
                    RestTemplate restTemplate = new RestTemplate();
                    List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
                    if (interceptors.isEmpty()) {
                        interceptors = new ArrayList<>();
                    }
                    interceptors.add(new LoggingInterceptor());
                    restTemplate.setInterceptors(interceptors);
                    String url = propertiesService.getPropertyValue("api.abbott.test.url");
                    URI uri = new URI(url);
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("apikey", "aymiAKqkN6RzmKhsvJhL");

                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    for (AbbottChaiDTO testObject : list) {
                        AbbottM2000 abbottTest = abbottM2000Repository.getById(testObject.getId());
                        try {
                            HttpEntity<AbbottChaiDTO> request = new HttpEntity<>(testObject, headers);
                            ResponseEntity<AbbottChaiResponseDTO> responseEntity = restTemplate.exchange(uri, HttpMethod.POST,
                                    request, AbbottChaiResponseDTO.class);
                            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                                abbottTest.setApiExported(true);
                                abbottTest.setApiResponse(200);
                                abbottTest.setExportDatetime(LocalDateTime.now());
                                abbottM2000Repository.save(abbottTest);
                                log.info("Abbott Test " + testObject.getSampleId() + "  successfully sent to the server");
                            }
                        } catch (HttpStatusCodeException e) {
                            log.severe(e.getRawStatusCode() + ": " + e.getResponseBodyAsString());
                            JsonObject convertedObject = new Gson().fromJson(e.getResponseBodyAsString(), JsonObject.class);
                            JsonElement messageElement = convertedObject.get("message");
                            if (e.getRawStatusCode() == 400) {
                                abbottTest.setApiExported(true);
                                abbottTest.setApiResponse(400);
                                abbottTest.setExportDatetime(LocalDateTime.now());
                                abbottM2000Repository.save(abbottTest);
                                log.info(
                                        "Abbott Test " + testObject.getSampleId() + "  was already accepted by the server");
                            } else {
                                abbottTest.setApiResponse(e.getRawStatusCode());
                                abbottM2000Repository.save(abbottTest);
                                log.severe("Failed to transmit Abbott Test  to the server. Response="
                                        + messageElement.getAsString());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
                break;
            case "ETHIOPIA":
                try {
                    List<EthiopiaCovidDTO> ethiopiaList = customRepository.getEthiopiaAbbottCovidNotSentData();
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
                                AbbottM2000 abbott = abbottM2000Repository.getById(dto.getId());
                                abbott.setApiExported(true);
                                abbottM2000Repository.save(abbott);
                                log.info("Test " + dto.getId() + " successfully sent to the server");
                            } else {
                                if (dto.getMsg().equals("Result already exists")) {
                                    AbbottM2000 abbott = abbottM2000Repository.getById(dto.getId());
                                    abbott.setApiExported(true);
                                    abbottM2000Repository.save(abbott);
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
