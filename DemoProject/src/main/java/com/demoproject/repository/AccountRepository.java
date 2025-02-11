package com.demoproject.repository;

import java.util.Collection;
import java.util.List;
import com.demoproject.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Account findByUsername(String username);

    List<Account> findByUserIdIn(Collection<Long> userIds);

    Account findById(Long id);
    Page<Account> findByUserIdInAndIsDeleteFalse(List<Long> userIds, Pageable pageable); // Chỉ lấy tài khoản chưa xóa

}
