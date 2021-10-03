package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.dto.NodePropertyDTO;
import com.edgedx.connectpoc.dto.NodePropertyUpdatedDTO;
import com.edgedx.connectpoc.entity.NodeConfiguration;
import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Log
public class NodeUpdateAPIServiceImpl implements NodeUpdateAPIService {

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Override
    public Boolean needToUpdate() {
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isPresent()) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getInterceptors().add(
                    new BasicAuthorizationInterceptor("connectpocuser", "dLhVHQPRLqp3MmbxVE4ZGwU3ymqNMsvMcSVJ"));
            String url = this.propertiesService.getPropertyValue("api.connectpoc.version.url");
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("version", propertiesService.getPropertyValue("connectpoc.current.version"))
                    .queryParam("node", nodeConfiguration.get().getFacility());
            ResponseEntity<Boolean> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
                    null, Boolean.class);
            return responseEntity.getBody();
        }
        return false;
    }

    @Override
    public List<NodePropertyDTO> getUpdatedProperties() {
        List<NodePropertyDTO> properties;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(
                new BasicAuthorizationInterceptor("connectpocuser", "dLhVHQPRLqp3MmbxVE4ZGwU3ymqNMsvMcSVJ"));
        String url = this.propertiesService.getPropertyValue("api.connectpoc.properties.url");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("node", nodeConfigurationRepository.findTopByOrderByFacilityDesc().get().getFacility());
        ResponseEntity<NodePropertyDTO[]> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
                null, NodePropertyDTO[].class);
        properties = Arrays.asList(responseEntity.getBody());
        return properties;
    }

    @Override
    public void postUpdatedProperties(List<NodePropertyDTO> properties) {
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if (nodeConfiguration.isPresent()) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getInterceptors().add(
                    new BasicAuthorizationInterceptor("connectpocuser", "dLhVHQPRLqp3MmbxVE4ZGwU3ymqNMsvMcSVJ"));
            String url = this.propertiesService.getPropertyValue("api.connectpoc.post.properties.url");
            String nodeId = nodeConfiguration.get().getFacility();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("node", nodeId);
            List<NodePropertyUpdatedDTO> updatedProperties = new ArrayList<>();
            for (NodePropertyDTO property : properties) {
                NodePropertyUpdatedDTO updated = new NodePropertyUpdatedDTO();
                updated.setNodeId(nodeId);
                updated.setPropertyName(property.getName());
                updated.setPropertyValue(property.getValue());
                updatedProperties.add(updated);
            }
            HttpEntity<List<NodePropertyUpdatedDTO>> request = new HttpEntity<>(updatedProperties);
            restTemplate.exchange(builder.toUriString(), HttpMethod.POST,
                    request, NodePropertyDTO[].class);
        }
    }
}
