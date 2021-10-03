package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.FacsprestoPatientSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacsprestoPatientSampleRepository extends JpaRepository<FacsprestoPatientSample, Long> {

}