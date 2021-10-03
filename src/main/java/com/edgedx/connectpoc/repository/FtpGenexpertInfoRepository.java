package com.edgedx.connectpoc.repository;

import com.edgedx.connectpoc.entity.FtpGenexpertInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FtpGenexpertInfoRepository extends JpaRepository<FtpGenexpertInfo, Long> {

    Optional<FtpGenexpertInfo> getTopByOrderByUploadTimeDesc();

}
