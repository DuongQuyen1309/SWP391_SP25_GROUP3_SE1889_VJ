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

    public BillRequest(int totalMoney, int paidMoney, int debtMoney,
                       String productData, String customerData, Long createdBy, boolean ported, boolean isDebt, Long storeId,boolean status) {

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
}

