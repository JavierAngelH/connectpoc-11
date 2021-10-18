package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.PimaCd4;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PimaCd4Repository extends JpaRepository<PimaCd4, Long> {

    List<PimaCd4> findAllByIsExportedFalse();

    Optional<PimaCd4> findTopByDeviceOrderByStartTimeDesc(String device);

}