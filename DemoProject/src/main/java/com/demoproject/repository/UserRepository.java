package com.demoproject.repository;

import com.demoproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findById(Long id);
    @Query("SELECT u.id FROM User u WHERE u.role = :role")
    List<Long> findIdByRole(@Param("role") String role);

}