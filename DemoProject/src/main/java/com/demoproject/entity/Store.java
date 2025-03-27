package com.demoproject.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "store")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, columnDefinition = "Numeric(18)")
    private Long id;
    @Column(name = "NAME", length = 50)
    private String name;
    @Column(name = "PHONE", length = 10, unique = true)
    private String phone;
    @Column(name = "ADDRESS", length = 50)

    private String address;
    @Column(name = "CREATED_BY")
    private Long createdBy;
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    @Column(name = "TAX_CODE", length = 20, unique = true)
    private String taxCode; // Thêm tax_code

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
