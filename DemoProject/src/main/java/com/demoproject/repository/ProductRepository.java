package com.demoproject.repository;

import com.demoproject.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {



    List<Integer> findAllByIsDeleted(int isDeleted);

    List<Product> getProductByIsDeleted(int isDeleted);
}
