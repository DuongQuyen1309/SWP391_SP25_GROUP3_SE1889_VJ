package com.demoproject.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.demoproject.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsernameAndIsDeleteFalse(String username);



    boolean existsByUsername(String username);

    boolean existsByUsernameAndIsDeleteFalse(String username);
    List<Account> findByUserIdIn(Collection<Long> userIds);

    Optional<Account> findById(Long id);
    Page<Account> findByUserIdInAndIsDeleteFalse(List<Long> userIds, Pageable pageable); // Chỉ lấy tài khoản chưa xóa

    @Query("SELECT a FROM Account a WHERE (LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND a.userId IN :userIds AND a.isDelete = false")
    Page<Account> searchOwnerAccounts(@Param("keyword") String keyword, @Param("userIds") List<Long> userIds, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE (LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND a.userId IN :userIds AND a.isDelete = false")
    Page<Account> searchStaffAccounts(@Param("keyword") String keyword, @Param("userIds") List<Long> userIds, Pageable pageable);

}
