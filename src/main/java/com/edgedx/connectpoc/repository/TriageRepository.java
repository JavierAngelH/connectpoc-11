package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.Triage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriageRepository extends JpaRepository<Triage,Long> {

    List<Triage> findAllByIsExportedFalse();

}
