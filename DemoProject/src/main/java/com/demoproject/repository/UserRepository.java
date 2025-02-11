package com.demoproject.repository;

import com.demoproject.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    Users findById(Long id);
    @Query("SELECT u.id FROM Users u WHERE u.role = :role")
    List<Long> findIdByRole(@Param("role") String role);

    @Query("SELECT u.role FROM Users u WHERE u.id = :userId")
    String findRoleByUserId(@Param("userId") Long userId);

}