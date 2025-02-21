package com.demoproject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demoproject.entity.Zone;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Zone save(Zone zone);

    List<Zone> findAll();

    Optional<Zone> findById(Long id);

    List<Zone> findByWarehouseName(String warehouseName);

    void deleteById(int id);

    Page<Zone> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Zone> findByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
    @Query("SELECT z FROM Zone z WHERE z.warehouseName = :warehouseName")
    Page<Zone> findAllByWarehouseName(String warehouseName, Pageable pageable);
    @Query("SELECT z FROM Zone z WHERE z.warehouseName = :warehouseName AND LOWER(z.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Zone> findByNameAndWarehouse(String name, String warehouseName, Pageable pageable);



}
