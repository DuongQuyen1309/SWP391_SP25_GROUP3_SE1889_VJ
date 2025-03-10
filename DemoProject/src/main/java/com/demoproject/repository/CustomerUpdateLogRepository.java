package com.demoproject.repository;

import com.demoproject.entity.CustomerUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerUpdateLogRepository extends JpaRepository<CustomerUpdateLog, Long> {
    List<CustomerUpdateLog> findByCustomerId(Long customerId);
}
