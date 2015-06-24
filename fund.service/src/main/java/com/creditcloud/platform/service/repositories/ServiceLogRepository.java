package com.creditcloud.platform.service.repositories;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;

import com.creditcloud.platform.service.entities.ServiceLog;

public interface ServiceLogRepository extends JpaRepository<ServiceLog, BigInteger> {

}
