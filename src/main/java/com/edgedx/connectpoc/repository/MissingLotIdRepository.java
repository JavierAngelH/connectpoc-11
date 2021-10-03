package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.MissingLotId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissingLotIdRepository extends JpaRepository<MissingLotId,Integer> {

}
