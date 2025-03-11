package com.demoproject.repository;

import com.demoproject.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {



    List<Integer> findAllByIsDeleted(int isDeleted);

    List<Product> getProductByIsDeleted(int isDeleted);
    Page<Product> findByNameContaining(String name, Pageable pageable);

    Page<Product> findByDescriptionContaining(String name, Pageable pageable);

    List<Product> findByNameContainingAndStoreId(String name, Long storeId);

    List<Product> findByZoneId(Long zoneId);

    List<Product> findByZoneIdAndNameContaining(Long zoneId, String name);

    List<Product> findByZoneIdAndPriceBetween(Long zoneId, Double minPrice, Double maxPrice);

    @Query(value = "SELECT * FROM Product " +
            "WHERE LOWER(name) LIKE LOWER(CONCAT('%', :productName, '%')) " +
            "AND store_id = :storeId " +
            "AND quantity > 0", nativeQuery = true)
    List<Product> findByNameAndStoreIdAndQuantityGreaterThanZero(String productName, Long storeId);


    Page<Product> findByStoreId(Long storeId, Pageable pageable);

    Optional<Product> findByIdAndStoreId(Long productId, Long storeId);

    List<Product> findByStoreId(Long storeId);
}
