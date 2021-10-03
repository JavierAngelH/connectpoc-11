package com.edgedx.connectpoc.tasks;

import com.edgedx.connectpoc.dto.NodePropertyDTO;
import com.edgedx.connectpoc.entity.Property;
import com.edgedx.connectpoc.repository.PropertiesRepository;
import com.edgedx.connectpoc.service.NodeService;
import com.edgedx.connectpoc.service.NodeUpdateAPIService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

@Component
@Log
public class VerifyVersionTask {

    @Autowired
    protected NodeUpdateAPIService nodeUpdateAPIService;

    @Autowired
    PropertiesRepository propertiesRepository;

    @Autowired
    NodeService nodeService;

    @Scheduled(cron = "${schedule.midnight.cron}")
    public void publish() throws URISyntaxException, UnsupportedEncodingException {
        log.info("Starting verify properties scheduled task");
        List<NodePropertyDTO> propertiesToUpdate = nodeUpdateAPIService.getUpdatedProperties();
        if(!propertiesToUpdate.isEmpty()){
            int updatedProperties = 0;
            log.info("need to update properties");
            for (NodePropertyDTO propertyDTO: propertiesToUpdate) {
                Property property = propertiesRepository.getByPropertyKey(propertyDTO.getName());
                property.setPropertyValue(propertyDTO.getValue());
                propertiesRepository.save(property);
                updatedProperties++;
            }
            if(updatedProperties == propertiesToUpdate.size()){
                nodeUpdateAPIService.postUpdatedProperties(propertiesToUpdate);
                log.info("Properties updated successfully");
            }
        }else{
            log.info("Properties are up to date");
        }
        log.info("Verifying version scheduled task");
        Boolean needToUpdate = nodeUpdateAPIService.needToUpdate();
        if (needToUpdate) {
            log.info("Node needs to be updated");
            nodeService.updateProject();
        } else
            log.info("Node is up to date");
    }
}
