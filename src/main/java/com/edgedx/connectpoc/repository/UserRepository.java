package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    User findByUsernameIgnoreCase(String username);
}
