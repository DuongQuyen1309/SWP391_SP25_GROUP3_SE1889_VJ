package com.demoproject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demoproject.entity.Zone;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Zone save(Zone zone);

    List<Zone> findAll();

    Optional<Zone> findById(Long id);

    List<Zone> findByWarehouseId(int warehouseId);

    void deleteById(int id);

    Page<Zone> findByName(String name, Pageable pageable);

}
