package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.entity.Device;

public interface DeviceService {

    void updateCartridgeByLotId(String lotId, Device device);

    void verifyRemainingInventoryByDevice(Device device);

    void sendCartridgeInventory();

    void updateCartridgeByDevice(Device device);

    void updateUsedCartridgeHistoric(String serialNumber, Integer month, Integer year);

}
