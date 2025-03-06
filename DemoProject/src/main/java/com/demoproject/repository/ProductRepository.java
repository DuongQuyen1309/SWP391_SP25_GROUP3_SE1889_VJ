package com.demoproject.repository;

import com.demoproject.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {



    List<Integer> findAllByIsDeleted(int isDeleted);

    List<Product> getProductByIsDeleted(int isDeleted);
    Page<Product> findByNameContaining(String name, Pageable pageable);

    Page<Product> findByDescriptionContaining(String name, Pageable pageable);

    List<Product> findByNameContainingAndStoreId(String name, Long storeId);
}
