package com.demoproject.repository;

import com.demoproject.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Page<Bill> findAll(Pageable pageable);
    @Query("SELECT b FROM Bill b WHERE b.storeId = :storeId")
    Page<Bill> findAllByStoreId(Long storeId, Pageable pageable);

    @Query("SELECT b FROM  Bill b WHERE b.storeId = :storeId AND b.id = :id")
    Page<Bill> findByBillIdAndStoreId(Long id, Long storeId, Pageable pageable);

    @Query(value = "SELECT * FROM Bill b WHERE LOWER(JSON_VALUE(b.customer_data, '$.name')) LIKE LOWER(CONCAT('%', :customerName, '%')) AND b.store_id = :storeId", nativeQuery = true)
    Page<Bill> findByNameAndStoreId(String customerName, Long storeId, Pageable pageable);
}
