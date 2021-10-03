package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.entity.*;
import com.edgedx.connectpoc.model.NotificationMessage;
import com.edgedx.connectpoc.repository.*;
import com.edgedx.connectpoc.views.Broadcaster;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Service
@Log
public class DeviceServiceImpl implements DeviceService {
    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    CartridgeInventoryRepository cartridgeInventoryRepository;

    @Autowired
    MissingLotIdRepository missingLotIdRepository;

    @Autowired
    MessageNotificationRepository messageNotificationRepository;

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    NodeConfigurationRepository nodeConfigurationRepository;

    @Autowired
    DeviceCartridgeHistoricRepository deviceCartridgeHistoricRepository;

    @Override
    public void updateCartridgeByLotId(String lotId, Device device) {
        Optional<CartridgeInventory> cartridgeInventoryOptional = cartridgeInventoryRepository.findByLotIdAndDevice(lotId, device);
        if (cartridgeInventoryOptional.isPresent()) {
            CartridgeInventory cartridge = cartridgeInventoryOptional.get();
            int total = cartridge.getQuantity();
            if (total > 1) {
                cartridge.setQuantity(total - 1);
                cartridgeInventoryRepository.save(cartridge);
            } else {
                cartridgeInventoryRepository.delete(cartridge);
            }
        } else {
            MissingLotId missingLotId = new MissingLotId();
            missingLotId.setLotIdNumber(lotId);
            missingLotId.setDeviceType(device.getDeviceType());
            missingLotIdRepository.save(missingLotId);
            Broadcaster.broadcast("Lot ID: " + lotId + " doesn't exist in the database. Please verify", NotificationVariant.LUMO_ERROR);
        }
    }

    @Override
    public void verifyRemainingInventoryByDevice(Device device) {
        List<CartridgeInventory> inventory = cartridgeInventoryRepository.findAllByDevice(device);
        if (inventory.isEmpty()) {
            String message = messageNotificationRepository.findByTriggerName(NotificationMessage.EMPTY_INVENTORY.getTriggerName()).getText()
                    .replace("#device", device.getDeviceType().getDescription() + ": " + device.getSerialNumber());
            Broadcaster.broadcast(message, NotificationVariant.LUMO_ERROR);
        } else {
            int limit = Integer.parseInt(propertiesService.getPropertyValue("inventory.notification.limit"));
            int total = 0;
            for (CartridgeInventory cartridgeInventory : inventory) {
                total = cartridgeInventory.getQuantity() + total;
            }
            if (total <= limit) {
                String message = messageNotificationRepository.findByTriggerName(NotificationMessage.RUNNING_OUT_OF_INVENTORY.getTriggerName())
                        .getText().replace("#device", device.getDeviceType().getDescription() + ": " +
                                device.getSerialNumber()).replace("#qty", total + "");
                Broadcaster.broadcast(message, NotificationVariant.LUMO_PRIMARY);
            }
        }
    }

    @Override
    public void sendCartridgeInventory() {
        try {
            List<CartridgeInventory> list = cartridgeInventoryRepository.findAll();
            for (CartridgeInventory cartridgeInventory : list) {
                cartridgeInventory.setDeviceSerialNumber(cartridgeInventory.getDevice().getSerialNumber());
            }
            if (!list.isEmpty()) {
                Optional<NodeConfiguration> config = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
                if (config.isPresent()) {
                    String facilityCode = config.get().getFacilityCode();
                    RestTemplate restTemplate = new RestTemplate();
                    String urlRoot = propertiesService.getPropertyValue("api.root.url");
                    String url = urlRoot + "saveFacilityInventory/" + facilityCode;
                    HttpHeaders headers = new HttpHeaders();
                    HttpEntity<List<CartridgeInventory>> request = new HttpEntity<>(list, headers);
                    ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
                            request, String.class);
                    String answer = responseEntity.getBody();
                    if (answer.equals("success"))
                        log.info("Cartridge Inventory successfully sent to the server");
                    else
                        log.warning("Failed to transmit cartridge inventory to the server. Check server log for more information");
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error trying to send cartridge inventory to server " + e.getMessage(), e);

        }

    }

    @Override
    public void updateCartridgeByDevice(Device device) {
        List<CartridgeInventory> list = cartridgeInventoryRepository.findAllByDevice(device);
        if (!list.isEmpty()) {
            CartridgeInventory cartridgeToUpdate = list.get(0);
            int total = cartridgeToUpdate.getQuantity();
            if (total > 1) {
                cartridgeToUpdate.setQuantity(total - 1);
                cartridgeInventoryRepository.save(cartridgeToUpdate);
            } else {
                cartridgeInventoryRepository.delete(cartridgeToUpdate);
            }
        }
    }

    @Override
    public void updateUsedCartridgeHistoric(String serialNumber, Integer month, Integer year) {
        DeviceCartridgeHistoricId id = new DeviceCartridgeHistoricId(serialNumber, month, year);
        Optional<DeviceCartridgeHistoric> historicOptional = deviceCartridgeHistoricRepository.findById(id);
        DeviceCartridgeHistoric historic;
        if(historicOptional.isEmpty()){
            historic = new DeviceCartridgeHistoric(id, 1);
        } else {
        historic = historicOptional.get();
        historic.setUsedCartridges(historic.getUsedCartridges() + 1);
        }
        deviceCartridgeHistoricRepository.save(historic);
    }
}
