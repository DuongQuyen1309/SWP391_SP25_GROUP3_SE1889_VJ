package com.demoproject.entity;

import com.demoproject.dto.CustomerDataDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;

import java.io.IOException;
import java.time.LocalDate;

@Entity
@Table(name = "Bill")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "Numeric(18)")
    private Long id;
    @Column(name = "note", columnDefinition = "nvarchar(max)")
    private String note;

    @Column(name = "discount")
    private Integer discount;

    @Column(name = "totalMoney")
    private int totalMoney;
    @Column(name = "paidMoney")
    private int paidMoney;
    @Column(name = "debtMoney")
    private int debtMoney;
    @Column(name = "productData")
    private String productData;
    @Column(name = "customerData")
    private String customerData;
    @Column(name = "createdBy")
    private Long createdBy;
    @Column(name = "createdAt")
    private LocalDate createdAt;
    @Column(name = "ported")
    private boolean ported;
    @Column(name = "isDebt")
    private boolean isDebt;
    @Column(name = "store_id")
    private Long storeId;
    @Column(name = "status")
    private boolean status;

    @Transient
    private CustomerDataDTO customer; // Dữ liệu JSON sẽ được chuyển thành Object
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(int totalMoney) {
        this.totalMoney = totalMoney;
    }

    public int getPaidMoney() {
        return paidMoney;
    }

    public void setPaidMoney(int paidMoney) {
        this.paidMoney = paidMoney;
    }

    public String getProductData() {
        return productData;
    }

    public void setProductData(String productData) {
        this.productData = productData;
    }

    public int getDebtMoney() {
        return debtMoney;
    }

    public void setDebtMoney(int debtMoney) {
        this.debtMoney = debtMoney;
    }

    public String getCustomerData() {
        return customerData;
    }

    public void setCustomerData(String customerData) {
        this.customerData = customerData;
        this.customer = parseCustomerData(customerData);
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

    public boolean isDebt() {
        return isDebt;
    }

    public void setDebt(boolean debt) {
        isDebt = debt;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public boolean isPorted() {
        return ported;
    }

    public void setPorted(boolean ported) {
        this.ported = ported;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public CustomerDataDTO getCustomer() {
        if (customer == null && customerData != null) {
            this.customer = parseCustomerData(customerData);
        }
        return customer;
    }

    private CustomerDataDTO parseCustomerData(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, CustomerDataDTO.class);
        } catch (IOException e) {
            return null;
        }
    }
}
