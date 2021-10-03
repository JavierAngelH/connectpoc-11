package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.GenexpertTestAnalyte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenexpertTestAnalyteRepository extends JpaRepository<GenexpertTestAnalyte, Long> {

    List<GenexpertTestAnalyte> findAllByTestId(Long testId);

}
