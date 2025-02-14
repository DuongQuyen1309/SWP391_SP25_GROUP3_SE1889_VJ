package com.demoproject.repository;

import com.demoproject.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c.id FROM Customer c WHERE c.isDelete = false")
    public List<Long> findAllActiveCustomerIds();

    @Query("SELECT c FROM Customer c WHERE c.id =: id")
    public Customer findByCustomerId(Long id);

    Page<Customer> findByIdInAndIsDeleteFalse(List<Long> customerIds,Pageable pageable);
}
