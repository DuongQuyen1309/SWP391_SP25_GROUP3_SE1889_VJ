package com.demoproject.repository;

import com.demoproject.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{

    Page<Product> findByNameContaining(String name, Pageable pageable);

    Page<Product> findByDescriptionContaining(String name, Pageable pageable);

    List<Product> findByNameContainingAndStoreId(String name, Long storeId);

    Page<Product> findByStoreId(Long storeId, Pageable pageable);

    List<Product> findByStoreId(Long storeId);

    Optional<Product> findByIdAndStoreId(Long productId, Long storeId);

    List<Product> findByNameContainingIgnoreCase(String name);

}
