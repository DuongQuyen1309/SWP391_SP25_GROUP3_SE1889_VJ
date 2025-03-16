package com.demoproject.repository;

import com.demoproject.entity.Customer;
import com.demoproject.entity.CustomerUpdateLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerUpdateLogRepository extends JpaRepository<CustomerUpdateLog, Long> {
    List<CustomerUpdateLog> findByCustomerId(Long customerId);

    @Query("SELECT c FROM CustomerUpdateLog c WHERE (c.storeId = :storeID) " +
            " AND (:updatedDateFrom IS NULL OR c.updatedAt >= :updatedDateFrom) " +
            " AND (:updatedDateTo IS NULL OR c.updatedAt <= :updatedDateTo) " +
            " AND (:informationField IS NULL OR c.fieldName = :informationField)" +
            " AND (:updatedBy IS NULL OR c.updatedBy = :updatedBy)" +
            " AND (:logStatus IS NULL OR c.status = :logStatus) " +
            " AND (:preValue IS NULL OR LOWER(c.oldValue) LIKE LOWER(CONCAT('%', :preValue, '%'))) " +
            " AND (:followValue IS NULL OR LOWER(c.newValue) LIKE LOWER(CONCAT('%', :followValue, '%')))")
    Page<CustomerUpdateLog> getUpdateLogByAttribute(@Param("storeID") Long storeId, @Param("informationField") String informationField,
                                           @Param("updatedBy") Long updatedBy, @Param("preValue") String preValue,
                                           @Param("followValue") String followValue, @Param("updatedDateFrom") LocalDateTime updatedDateFrom,
                                           @Param("updatedDateTo") LocalDateTime updatedDateTo, @Param("logStatus") String logStatus,
                                           Pageable pageable);
    @Query("SELECT c FROM CustomerUpdateLog c WHERE (c.storeId = :storeID)")
    Page<CustomerUpdateLog> getAllUpdateLog(@Param("storeID") Long storeId, Pageable pageable);

}
