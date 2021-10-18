package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.Genexpert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface GenexpertRepository extends JpaRepository<Genexpert, Long> {

    Optional<Genexpert> findTopByOrderByStartTimeDesc();

    Integer countByStartTimeIsAfterAndStartTimeIsBefore(LocalDateTime start, LocalDateTime end);

    Integer countByStartTimeIsAfterAndStartTimeIsBeforeAndErrorIsNotNullAndErrorNotIn(LocalDateTime start, LocalDateTime end, Collection<String> errors);

    Integer countByErrorIsNotNullAndErrorIsNotNullAndErrorNotIn(Collection<String> errors);

    List<Genexpert> findAllByIsExportedFalse();

    Optional<Genexpert> findTopByDeviceNameOrderByStartTimeDesc(String deviceName);
}
