package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.FacsprestoInstrumentQc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacsprestoInstrumentQcRepository extends JpaRepository<FacsprestoInstrumentQc, Long> {

    List<FacsprestoInstrumentQc> findAllByIsExportedFalse();
}