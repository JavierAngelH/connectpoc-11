package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.NodeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NodeConfigurationRepository extends JpaRepository<NodeConfiguration, String> {

    Optional<NodeConfiguration> findTopByOrderByFacilityDesc();

}
