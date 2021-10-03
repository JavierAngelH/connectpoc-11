package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.AbbottM2000;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbbottM2000Repository extends JpaRepository<AbbottM2000, Long> {

    Optional<AbbottM2000> findTopByOrderByTestDatetimeDesc();

    Integer countByTestDatetimeIsAfterAndTestDatetimeIsBefore(LocalDateTime start, LocalDateTime end);

    Integer countByTestDatetimeIsAfterAndTestDatetimeIsBeforeAndTestErrorIsNotNullAndTestErrorNotIn(LocalDateTime start, LocalDateTime end, Collection<String> errors);

    Integer countByTestErrorIsNotNullAndTestErrorIsNotNullAndTestErrorNotIn(Collection<String> errors);

    List<AbbottM2000> findAllByIsExportedFalse();
}
