package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.CartridgeInventory;
import com.edgedx.connectpoc.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartridgeInventoryRepository extends JpaRepository<CartridgeInventory, String> {

    List<CartridgeInventory> findAllByDevice(Device device);

    Optional<CartridgeInventory> findByLotIdAndDevice(String lotId, Device device);
}
