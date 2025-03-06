package com.demoproject.repository;

import com.demoproject.entity.Store;
import com.demoproject.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
