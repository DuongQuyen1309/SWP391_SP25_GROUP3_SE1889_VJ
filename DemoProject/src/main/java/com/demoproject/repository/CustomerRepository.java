package com.demoproject.repository;

import com.demoproject.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c.id FROM Customer c WHERE c.isDelete = false")
    public List<Long> findAllActiveCustomerIds();

    @Query("SELECT c FROM Customer c WHERE c.id =: id")
    public Customer findByCustomerId(Long id);

    Page<Customer> findByIdInAndIsDeleteFalse(List<Long> customerIds,Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.id IN :ids AND c.isDelete = false AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Customer> findByIdInAndIsDeleteFalseAndNameContainingIgnoreCase(@Param("ids") List<Long> ids, @Param("name") String name, Pageable pageable);

    @Query("SELECT DISTINCT c.ctype FROM Customer c WHERE c.isDelete = false")
    List<String> findDistinctCtypes();

    @Query("SELECT c FROM Customer c WHERE c.id IN :ids AND c.isDelete = false " +
            "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:ctype IS NULL OR LOWER(c.ctype) LIKE LOWER(CONCAT('%', :ctype, '%')))")
    Page<Customer> findByIdInAndIsDeleteFalseAndNameAndCtype(@Param("ids") List<Long> ids,
                                                             @Param("name") String name,
                                                             @Param("ctype") String ctype,
                                                             Pageable pageable);
}
