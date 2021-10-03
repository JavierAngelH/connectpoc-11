/**
 * CartridgeInventoryImpl.java Created: Mar 12, 2018 JavierAngelH
 */

package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.entity.CartridgeInventory;
import com.edgedx.connectpoc.entity.Device;
import com.edgedx.connectpoc.entity.NodeConfiguration;
import com.edgedx.connectpoc.repository.CartridgeInventoryRepository;
import com.edgedx.connectpoc.repository.DeviceRepository;
import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import lombok.extern.java.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


@Service
@Log
public class CartridgeInventoryAPIServiceImpl implements CartridgeInventoryAPIService {

    @Autowired
    CartridgeInventoryRepository cartridgeInventoryRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Override
    public void sendCartridgeInventory() {
        try {
            List<CartridgeInventory> list = cartridgeInventoryRepository.findAll();
            for (CartridgeInventory cartridgeInventory : list) {
                cartridgeInventory.setDeviceSerialNumber(cartridgeInventory.getDevice().getSerialNumber());
            }
            if (!list.isEmpty()) {
                NodeConfiguration config = nodeConfigurationRepository.findTopByOrderByFacilityDesc().get();
                String facilityCode = config.getFacilityCode();
                RestTemplate restTemplate = new RestTemplate();
                String urlRoot = propertiesService.getPropertyValue("api.root.url");
                String url = urlRoot + "saveFacilityInventory/{code}";
                Map<String, String> map = new HashMap<String, String>();
                map.put("code", facilityCode);
                String answer = restTemplate.postForObject(url, list, String.class, map);
                if (answer.equals("success"))
                    log.info("Cartridge Inventory successfully sent to the server");
                else
                    log.info("Failed to transmit cartridge inventory to the server. Check server log for more information");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error trying to send cartridge inventory to server ", e);

        }

    }

}
