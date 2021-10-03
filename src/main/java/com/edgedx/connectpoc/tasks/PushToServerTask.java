package com.edgedx.connectpoc.tasks;

import com.edgedx.connectpoc.service.PropertiesService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log
public class PushToServerTask {

    @Autowired
    PropertiesService propertiesService;

    public void publish(){

    }
}
