package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertiesRepository extends JpaRepository<Property,Long> {

    List<Property> findAllByScope(String scope);

    Property getByPropertyKey(String key);

}
