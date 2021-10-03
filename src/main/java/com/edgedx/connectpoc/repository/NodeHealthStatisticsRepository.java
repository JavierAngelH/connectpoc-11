package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.NodeHealthStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeHealthStatisticsRepository extends JpaRepository<NodeHealthStatistics, Long> {

    Optional<NodeHealthStatistics> findTopByOrderByLogDateDesc();

    List<NodeHealthStatistics> findAllByIsExportedFalse();

    List<NodeHealthStatistics> findTop2ByOrderByLogDateDesc();

}
