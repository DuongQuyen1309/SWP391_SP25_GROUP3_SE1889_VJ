package com.demoproject.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsernameAndIsDeleteFalse(String username);

    Optional<Account> findByEmailAndIsDeleteFalse(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailAndIsDeleteFalse(String email);


    boolean existsByUsernameAndIsDeleteFalse(String username);
    List<Account> findByUserIdIn(Collection<Long> userIds);

    Optional<Account> findById(Long id);
    Page<Account> findByUserIdInAndIsDeleteFalse(List<Long> userIds, Pageable pageable); // Chỉ lấy tài khoản chưa xóa
    @Query("SELECT a FROM Account a WHERE a.storeId = :storeId")
    Optional<Account> getOwnerNameByStoreId(@Param("storeId") Long storeId);
    @Query("SELECT a FROM Account a WHERE (LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')))"+
            "AND a.userId IN :userIds AND a.isDelete = false")
    Page<Account> searchOwnerAccounts(@Param("keyword") String keyword, @Param("userIds") List<Long> userIds, Pageable pageable);

    @Query("SELECT a FROM Account a " +
            "JOIN Users u ON a.userId = u.id " +
            "WHERE (LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND a.userId IN :userIds AND a.isDelete = false")
    Page<Account> searchStaffAccounts(@Param("keyword") String keyword, @Param("userIds") List<Long> userIds, Pageable pageable);


    Optional<Account> findByResetTokenAndIsDeleteFalse(String resetToken);

    @Query("SELECT a FROM Account a " +
            "JOIN Users u ON a.userId = u.id " +
            "WHERE (a.userId IN :ids) AND (a.isDelete = false) "+
            " AND (:req_idFrom IS NULL OR a.id >= :req_idFrom) " +
            " AND (:req_idTo IS NULL OR a.id <= :req_idTo) " +
            " AND (:req_username IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :req_username, '%'))) " +
            " AND (:req_displayName IS NULL OR LOWER(a.displayName) LIKE LOWER(CONCAT('%', :req_displayName, '%')))" +
            " AND (:req_email IS NULL OR LOWER(a.email) LIKE LOWER(CONCAT('%', :req_email, '%')))" +
            " AND (:req_fullname IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :req_fullname, '%')))")
    Page<Account> findByAttribute(@Param("ids") List<Long> ids,
                                   @Param("req_idFrom") Long req_idFrom, @Param("req_idTo") Long req_idTo,
                                   @Param("req_username") String req_username,
                                   @Param("req_displayName") String req_displayName,
                                   @Param("req_email") String req_email,
                                   @Param("req_fullname") String req_fullname, Pageable pageable);

}
