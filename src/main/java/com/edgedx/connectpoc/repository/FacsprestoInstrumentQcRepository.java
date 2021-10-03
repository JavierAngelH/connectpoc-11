package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.FacsprestoInstrumentQc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacsprestoInstrumentQcRepository extends JpaRepository<FacsprestoInstrumentQc, Long> {

}