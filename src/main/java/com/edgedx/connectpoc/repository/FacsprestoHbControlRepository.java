package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.FacsprestoHbControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacsprestoHbControlRepository extends JpaRepository<FacsprestoHbControl, Long> {

}