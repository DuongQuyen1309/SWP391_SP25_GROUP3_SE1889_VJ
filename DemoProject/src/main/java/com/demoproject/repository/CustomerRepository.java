package com.demoproject.repository;

import com.demoproject.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c.id FROM Customer c WHERE c.isDelete = false")
    public List<Long> findAllActiveCustomerIds();

    @Query("SELECT c FROM Customer c WHERE c.id =: id")
    public Customer findByCustomerId(Long id);

    @Query("SELECT c FROM Customer c WHERE c.createdBy IN :ids AND c.isDelete = false ")
    Page<Customer> findByIdInAndIsDeleteFalse(@Param("ids") List<Long> ids,Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.id IN :ids AND c.isDelete = false AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Customer> findByIdInAndIsDeleteFalseAndNameContainingIgnoreCase(@Param("ids") List<Long> ids, @Param("name") String name, Pageable pageable);

    @Query("SELECT DISTINCT c.ctype FROM Customer c WHERE c.isDelete = false")
    List<String> findDistinctCtypes();


    @Query("SELECT c FROM Customer c WHERE (c.createdBy IN :ids) AND (c.isDelete = false) " +
            " AND (:req_idFrom IS NULL OR c.id >= :req_idFrom) " +
            " AND (:req_idTo IS NULL OR c.id <= :req_idTo) " +
            " AND (:req_moneyFrom IS NULL OR c.moneyState >= :req_moneyFrom) " +
            " AND (:req_moneyTo IS NULL OR c.moneyState <= :req_moneyTo) " +
            " AND (:req_phone IS NULL OR c.phone = :req_phone)" +
            " AND (:req_customerType IS NULL OR c.ctype = :req_customerType)" +
            " AND (:dobFrom IS NULL OR c.dob >= :dobFrom) " +
            " AND (:dobTo IS NULL OR c.dob <= :dobTo) " +
            " AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            " AND (:req_address IS NULL OR LOWER(c.address) LIKE LOWER(CONCAT('%', :req_address, '%')))")
    Page<Customer> findByAttribute(@Param("ids") List<Long> ids,
                                   @Param("req_idFrom") Long req_idFrom, @Param("req_idTo") Long req_idTo,
                                   @Param("req_moneyFrom") Integer req_moneyFrom, @Param("req_moneyTo")Integer req_moneyTo,
                                   @Param("req_phone") String req_phone, @Param("dobFrom") LocalDate dobFrom,
                                   @Param("dobTo") LocalDate dobTo, @Param("req_customerType") String req_customerType,
                                   @Param("req_address") String req_address, @Param("name") String name, Pageable pageable);
    @Query("SELECT c.phone FROM Customer c WHERE c.createdBy IN :ids ")
    List<String> getAllPhoneNumbers(@Param("ids") List<Long> ids );
}
