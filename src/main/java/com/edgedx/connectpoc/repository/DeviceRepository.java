package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.Device;
import com.edgedx.connectpoc.model.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DeviceRepository extends JpaRepository<Device,Integer> {

    Optional<Device> getDeviceBySerialNumber(String serialNumber);

    Optional<Device> getTopByDeviceTypeOrderByIdDesc(DeviceType deviceType);

    List<Device> findAllByIsExportedFalse();

}
