package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.FacsprestoPatientSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacsprestoPatientSampleRepository extends JpaRepository<FacsprestoPatientSample, Long> {

    List<FacsprestoPatientSample> findAllByIsExportedFalse();

    Optional<FacsprestoPatientSample> findTopByDeviceCodeOrderByRunDatetimeDesc(String deviceCode);
}