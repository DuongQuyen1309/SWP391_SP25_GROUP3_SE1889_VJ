package com.demoproject.repository;

import com.demoproject.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findById(Long id);
    @Query("SELECT u.id FROM Users u WHERE u.role = :role")
    List<Long> findIdByRole(@Param("role") String role);

    @Query("SELECT u.role FROM Users u WHERE u.id = :userId")
    String findRoleByUserId(@Param("userId") Long userId);


    boolean existsByPhoneAndIdNot(String phone, Long id);

    Optional<Users> findByPhone(String phone);
    @Query("SELECT u.id FROM Users u WHERE u.storeId = :storeId "+
            "AND u.role='STAFF'"
    )
    List<Long> getStaffID(Long storeId);

    @Query("SELECT u.createdBy FROM Users u WHERE u.id = :staffID")
    Long getOwnerID(Long staffID);

    @Query("SELECT u FROM Users u WHERE u.id IN :ids ")
    List<Users> getUser(@Param("ids") List<Long> ids);

    @Query("SELECT u.id FROM Users u WHERE u.createdBy = :ownerID ")
    List<Long> getStaffID1(Long ownerID);

    @Query("SELECT u FROM Users u WHERE u.storeId = :storeID")
    List<Users> getUserInStore(Long storeID);
}