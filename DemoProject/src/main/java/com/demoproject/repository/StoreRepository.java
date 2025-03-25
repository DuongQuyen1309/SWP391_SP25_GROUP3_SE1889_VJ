package com.demoproject.repository;

import com.demoproject.entity.Store;
import com.demoproject.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, Long>, JpaSpecificationExecutor<Store> {
    Page<Store> findByNameOrPhone (String name, String phone, Pageable pageable);
    @Query("SELECT s FROM Store s WHERE (:name IS NULL OR s.name LIKE %:name%) " +
            "AND (:phone IS NULL OR s.phone LIKE %:phone%)")
    Page<Store> findStores(@Param("name") String name,
                           @Param("phone") String phone,
                           Pageable pageable);

    @Query("SELECT a.displayName FROM Account a WHERE a.id = (SELECT s.storeId FROM Store s WHERE s.id = :storeId)")
    String findOwnerNameByStoreId(@Param("storeId") Long storeId);

    Store findByStoreId(Long storeId); // Tìm store theo storeId

}
