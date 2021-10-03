package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.DeviceCartridgeHistoric;
import com.edgedx.connectpoc.entity.DeviceCartridgeHistoricId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceCartridgeHistoricRepository extends JpaRepository<DeviceCartridgeHistoric, DeviceCartridgeHistoricId> {
}