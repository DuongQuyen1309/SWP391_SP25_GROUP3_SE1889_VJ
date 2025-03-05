package com.demoproject.queuenotemoney;

public class TransactionRequest {
    private Long customerId;
    private Integer amount;
    private boolean isDebt; // true: nợ, false: thanh toán
    private String notename;
    private Long createdByID;
    public TransactionRequest(Long customerId, Integer amount, boolean isDebt, String notename, Long createdByID) {
        this.customerId = customerId;
        this.amount = amount;
        this.isDebt = isDebt;
        this.notename = notename;
        this.createdByID = createdByID;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Integer getAmount() {
        return amount;
    }

    public boolean isDebt() {
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

    public void setDebt(boolean debt) {
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
}

