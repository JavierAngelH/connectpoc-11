package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.PimaBead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PimaBeadRepository extends JpaRepository<PimaBead, Long> {

    List<PimaBead> findAllByIsExportedFalse();
}