package com.demoproject.repository;

import com.demoproject.dto.response.ImportedNoteDetailResponse;
import com.demoproject.entity.ImportedNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ImportedNoteRepository extends JpaRepository<ImportedNote, Long> {

    Page<ImportedNote> findByStoreId(Long storeId, Pageable pageable);

    @Query("SELECT i FROM ImportedNote i " +
            "JOIN i.customer c " +
            "WHERE (:minId IS NULL OR i.id >= :minId) " +
            "AND (:maxId IS NULL OR i.id <= :maxId) " +
            "AND (:dateFrom IS NULL OR i.createdAt >= :dateFrom) " +
            "AND (:dateTo IS NULL OR i.createdAt <= :dateTo) " +
            "AND (:minMoney <= i.totalCost AND :maxMoney >= i.totalCost) " +
            "AND (:supplier IS NULL OR c.name LIKE %:supplier%)")
    Page<ImportedNote> findByFilters(
            Long minId, Long maxId,
            LocalDate dateFrom, LocalDate dateTo,
            Double minMoney, Double maxMoney,
            String supplier, Pageable pageable);


    @Query("SELECT i.id, i.totalCost, i.createdAt, i.paidMoney,i.portedMoney,i.debtMoney, " +
            "c.name AS customerName, c.phone AS customerPhone, c.address AS customerAddress " +
            "FROM ImportedNote i " +
            "JOIN Customer c ON i.customerId = c.id " +
            "WHERE i.id = :id")
    ImportedNote findImportedNoteWithCustomerById(Long id);

    @Query("SELECT i FROM ImportedNote i JOIN FETCH i.customer WHERE i.id = :id")
    Optional<ImportedNote> findImportedNoteWithCustomer(@Param("id") Long id);

}
