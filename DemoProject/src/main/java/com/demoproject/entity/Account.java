package com.demoproject.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, columnDefinition = "Numeric(18)")
    private Long id;
    @Column(name = "USERNAME", length = 50, nullable = false, unique = true)
    private String username;
    @Column(name = "PASSWORD", length = 255, nullable = false)
    private String password;
    @Column(name = "DISPLAYNAME", length = 50, nullable = false)
    private String displayName;
    @Column(name = "createdBy")
    private Long createdBy;

    @Column(name = "createdAt")
    private LocalDate createdAt;

    @Column(name = "updatedAt")
    private LocalDate updatedAt;

    @Column(name = "deletedAt")
    private LocalDate deletedAt;

    @Column(name = "isDelete")
    private Boolean isDelete = false;

    @Column(name = "user_id")
    private Long userId; // ✅ Thêm trường liên kết với User


    public Account() {
    }

    public Account(String username, Long id, String password, String displayName) {
        this.username = username;
        this.id = id;
        this.password = password;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDate getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDate deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Boolean getDelete() {
        return isDelete;
    }

    public void setDelete(Boolean delete) {
        isDelete = delete;
    }
}


