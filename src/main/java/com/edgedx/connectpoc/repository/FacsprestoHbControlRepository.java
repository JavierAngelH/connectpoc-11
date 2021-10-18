package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.FacsprestoHbControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacsprestoHbControlRepository extends JpaRepository<FacsprestoHbControl, Long> {

    List<FacsprestoHbControl> findAllByIsExportedFalse();
}