package com.demoproject.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_gen")
    @SequenceGenerator(name = "account_gen",sequenceName = "account_id", allocationSize = 1)
    @Column(name = "ID", nullable = false, columnDefinition = "Numeric(18)")
    private Long id;
    @Column(name = "USERNAME", length = 50, nullable = false)
    private String username;
    @Column(name = "PASSWORD", length = 50, nullable = false)
    private String password;
    @Column(name = "DISPLAYNAME", length = 50, nullable = false)
    private String displayName;

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
}


