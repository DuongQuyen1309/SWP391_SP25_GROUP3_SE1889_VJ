package com.demoproject.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.demoproject.entity.Package;

public interface PackageRepository extends JpaRepository<Package, Long>, JpaSpecificationExecutor {
    List<Package> findAll();

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<Package> findByName(String name);

    @Query("SELECT p FROM Package p WHERE p.storeId = :storeId")
    Page<Package> findAllByStoreId(Long storeId, Pageable pageable);

    @Query("SELECT p FROM Package p WHERE p.storeId = :storeId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Package> findByNameAndStoreId(String name, Long storeId, Pageable pageable);

    @Query("SELECT p FROM Package p WHERE p.storeId = :storeId AND LOWER(p.color) LIKE LOWER(CONCAT('%', :color, '%'))")
    Page<Package> findByColorAndStoreId(String color, Long storeId, Pageable pageable);

    Optional<Package> findById(Long Id);

    @Query("SELECT p FROM Package p WHERE p.storeId = :storeId AND LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    Page<Package> findByDescriptionAndStoreId(String description, Long storeId, Pageable pageable);

    Page<Package> findByStoreIdAndCreatedAtBetween(Long storeId, LocalDate startDate,
                                                   LocalDate endDate, Pageable pageable);
}