package com.demoproject.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demoproject.entity.Product;
import com.demoproject.entity.Zone;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long>, JpaSpecificationExecutor {
    Zone save(Zone zone);

    List<Zone> findAll();

    Optional<Zone> findById(Long id);

    void deleteById(int id);

    Page<Zone> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Zone> findByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT z FROM Zone z WHERE z.storeId = :storeId")
    Page<Zone> findAllByStoreId(Long storeId, Pageable pageable);

    @Query("SELECT z FROM Zone z WHERE z.storeId = :storeId AND LOWER(z.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Zone> findByNameAndStoreId(String name, Long storeId, Pageable pageable);

    @Query("SELECT z FROM Zone z WHERE z.storeId = :storeId AND LOWER(z.position) LIKE LOWER(CONCAT('%', :position, '%'))")
    Page<Zone> findByPositionAndStoreId(String position, Long storeId, Pageable pageable);

    boolean existsByNameAndStoreId(String name, Long storeId);

    List<Zone> findByStoreId(Long storeId);

    @Query("SELECT z.product FROM Zone z WHERE z.id = :zoneId")
    Product findProductByZoneId(Long zoneId);

    @Query("SELECT z FROM Zone z WHERE z.createdAt BETWEEN :startDate AND :endDate AND z.storeId = :storeId")
    Page<Zone> findByCreatedAtBetweenAndStoreId(LocalDate startDate, LocalDate endDate, Long storeId,
                                                Pageable pageable);

    Zone getZoneById(Long id);

}
