package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.dto.NodePropertyDTO;

import java.util.List;

public interface NodeUpdateAPIService {

    Boolean needToUpdate();

    List<NodePropertyDTO> getUpdatedProperties();

    void postUpdatedProperties(List<NodePropertyDTO> properties);
}
