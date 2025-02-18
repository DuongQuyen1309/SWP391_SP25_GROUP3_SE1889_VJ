package com.demoproject.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "Note")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "Numeric(18)")
    private Long id;
    @Column(name = "isDebt")
    private Boolean isDebt;
    @Column(name = "customerId")
    private Long customerId;
    @Column(name = "createdBy")
    private Integer createdBy;
    @Column(name = "createdAt")
    private LocalDate createdAt;
    @Column(name = "note")
    private String note;
    @Column(name = "money")
    private int money;

    public Note() {
    }

    public Note(Boolean isDebt, Long customerId, String note, int money) {
        this.isDebt = isDebt;
        this.customerId = customerId;
        this.note = note;
        this.money = money;
    }

    public Note(Long id, Boolean isDebt, Long customerId, String note, int money) {
        this.id = id;
        this.isDebt = isDebt;
        this.customerId = customerId;
        this.note = note;
        this.money = money;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getDebt() {
        return isDebt;
    }

    public void setDebt(Boolean debt) {
        isDebt = debt;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }
}
