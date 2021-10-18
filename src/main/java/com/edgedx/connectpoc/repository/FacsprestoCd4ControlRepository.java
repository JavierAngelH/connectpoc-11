package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.FacsprestoCd4Control;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacsprestoCd4ControlRepository extends JpaRepository<FacsprestoCd4Control, Long> {

    List<FacsprestoCd4Control> findAllByIsExportedFalse();
}