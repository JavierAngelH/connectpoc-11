package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.Mgit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MgitRepository extends JpaRepository<Mgit,Long> {

    List<Mgit> findAllByIsExportedFalse();
}
