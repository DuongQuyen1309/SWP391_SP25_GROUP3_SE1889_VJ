package com.demoproject.queuenotemoney;

import jakarta.persistence.Column;

public class TransactionRequest {
    private Long customerId;
    private Integer amount;
    private String isDebt; // true: nợ, false: thanh toán
    private String notename;
    private Long createdByID;
    private Long storeId;
    private String imagePath;
    public TransactionRequest(Long customerId, Integer amount, String isDebt, String notename, Long createdByID, Long storeId, String imagePath) {
        this.customerId = customerId;
        this.amount = amount;
        this.isDebt = isDebt;
        this.notename = notename;
        this.createdByID = createdByID;
        this.storeId = storeId;
        this.imagePath = imagePath;
    }

    public TransactionRequest(Long customerId, Integer amount, String isDebt, String notename, Long createdByID, Long storeId) {
        this.customerId = customerId;
        this.amount = amount;
        this.isDebt = isDebt;
        this.notename = notename;
        this.createdByID = createdByID;
        this.storeId = storeId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Integer getAmount() {
        return amount;
    }

    public String isDebt() {
        return isDebt;
    }
    public String getNotename() {
        return notename;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setDebt(String debt) {
        isDebt = debt;
    }

    public void setNotename(String notename) {
        this.notename = notename;
    }

    public Long getCreatedByID() {
        return createdByID;
    }

    public void setCreatedByID(Long createdByID) {
        this.createdByID = createdByID;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}

