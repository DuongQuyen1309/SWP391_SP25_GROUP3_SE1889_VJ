package com.demoproject.billqueue;

public class BillRequest {
    private Long billId;
    private int totalMoney;
    private int paidMoney;
    private int debtMoney;
    private String productData;
    private String customerData;
    private Long createdBy;
    private boolean ported;  // Tiền bốc hàng
    private boolean isDebt;
    private Long storeId;
    private boolean status;
    private String note;
    private int discount;
    private int portedMoney;

    public BillRequest(int totalMoney, int paidMoney, int debtMoney,
                       String productData, String customerData, Long createdBy, boolean ported, boolean isDebt, Long storeId,boolean status,String note,
                       int discount,int portedMoney) {

        this.totalMoney = totalMoney;
        this.paidMoney = paidMoney;
        this.debtMoney = debtMoney;
        this.productData = productData;
        this.customerData = customerData;
        this.createdBy = createdBy;
        this.ported = ported;
        this.isDebt = isDebt;
        this.storeId = storeId;
        this.status = status;
        this.note = note;
        this.discount = discount;
        this.portedMoney = portedMoney;
    }

    // Getters
    public Long getBillId() { return billId; }
    public int getTotalMoney() { return totalMoney; }
    public int getPaidMoney() { return paidMoney; }
    public int getDebtMoney() { return debtMoney; }
    public String getProductData() { return productData; }
    public String getCustomerData() { return customerData; }
    public Long getCreatedBy() { return createdBy; }
    public boolean isPorted() { return ported; }
    public boolean isDebt() { return isDebt; }
    public Long getStoreId() { return storeId; }

    public boolean isStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public int getDiscount() {
        return discount;
    }

    public int getPortedMoney() {
        return portedMoney;
    }

    // Setters

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public void setTotalMoney(int totalMoney) {
        this.totalMoney = totalMoney;
    }

    public void setPaidMoney(int paidMoney) {
        this.paidMoney = paidMoney;
    }

    public void setDebtMoney(int debtMoney) {
        this.debtMoney = debtMoney;
    }

    public void setProductData(String productData) {
        this.productData = productData;
    }

    public void setCustomerData(String customerData) {
        this.customerData = customerData;
    }

    public void setPorted(boolean ported) {
        this.ported = ported;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public void setDebt(boolean debt) {
        isDebt = debt;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public void setPortedMoney(int portedMoney) {
        this.portedMoney = portedMoney;
    }
}

