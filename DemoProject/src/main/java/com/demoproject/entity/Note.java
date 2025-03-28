package com.demoproject.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Note")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "Numeric(18)")
    private Long id;
    @Column(name = "isDebt")
    private String isDebt;
    @Column(name = "customerId")
    private Long customerId;
    @Column(name = "createdBy")
    private Long createdBy;
    @Column(name = "createdAt")
    private LocalDateTime createdAt;
    @Column(name = "note")
    private String note;
    @Column(name = "money")
    private int money;
    @Column(name = "store_id")
    private Long storeId;
    @Column(name = "image_path")
    private String imagePath;

    public Note() {
    }

    public Note(String isDebt, Long customerId, String note, int money) {
        this.isDebt = isDebt;
        this.customerId = customerId;
        this.note = note;
        this.money = money;
    }

    public Note(Long id, String isDebt, Long customerId, String note, int money) {
        this.id = id;
        this.isDebt = isDebt;
        this.customerId = customerId;
        this.note = note;
        this.money = money;
    }
    public Note(Long id, String isDebt, Long customerId, String note, int money, Long storeId, String imagePath) {
        this.id = id;
        this.isDebt = isDebt;
        this.customerId = customerId;
        this.note = note;
        this.money = money;
        this.storeId = storeId;
        this.imagePath = imagePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDebt() {
        return isDebt;
    }

    public void setDebt(String debt) {
        isDebt = debt;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getIsDebt() {
        return isDebt;
    }

    public void setIsDebt(String isDebt) {
        this.isDebt = isDebt;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
