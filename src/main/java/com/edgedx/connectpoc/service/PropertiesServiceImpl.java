package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.repository.PropertiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PropertiesServiceImpl implements PropertiesService {

    @Autowired
    PropertiesRepository propertiesRepository;

    @Override
    public String getPropertyValue(String key) {
        return propertiesRepository.getByPropertyKey(key).getPropertyValue();
    }

    @Override
    public String getFullPropertyValue(String key) {
        return propertiesRepository.getByPropertyKey("root.folder.path").getPropertyValue() +
                propertiesRepository.getByPropertyKey(key).getPropertyValue();
    }

}
