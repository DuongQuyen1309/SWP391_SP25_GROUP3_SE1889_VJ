package com.demoproject.entity;



import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Warehousezone")
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;
    @Column(name = "NAME", length = 50)
    private String name;
    @Column(name = "PRODUCT_ID", nullable = false)
    private int productId;
    @Column(name = "WAREHOUSE_NAME")
    private String warehouseName;
    @Column(name = "AMOUNT")
    private int amount;
    @Column(name = "CREATED_AT")
    private LocalDate createdAt;
    @Column(name = "CREATED_BY")
    private int createdBy;
    public Zone() {
    }

    public Zone(Long id, String name, int productId, String warehouseName, int amount, LocalDate createdAt) {
        this.id = id;
        this.name = name;
        this.productId = productId;
        this.warehouseName = warehouseName;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

}
