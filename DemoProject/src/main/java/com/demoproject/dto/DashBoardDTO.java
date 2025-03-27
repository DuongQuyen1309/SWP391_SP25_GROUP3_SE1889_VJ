package com.demoproject.dto;

public class DashBoardDTO {

    private long totalRevenue;
    private Long todayRevenue;
    private Long todayBillCount;
    // Getter/Setter đầy đủ
    private Long yesterdayRevenue;
    private Long yesterdayBillCount;
    private double revenueChangePercentage;
    private Long lowStockCount;


    public Long getYesterdayRevenue() {
        return yesterdayRevenue;
    }

    public void setYesterdayRevenue(Long yesterdayRevenue) {
        this.yesterdayRevenue = yesterdayRevenue;
    }

    public Long getYesterdayBillCount() {
        return yesterdayBillCount;
    }

    public void setYesterdayBillCount(Long yesterdayBillCount) {
        this.yesterdayBillCount = yesterdayBillCount;
    }

    public double getRevenueChangePercentage() {
        return revenueChangePercentage;
    }

    public void setRevenueChangePercentage(double revenueChangePercentage) {
        this.revenueChangePercentage = revenueChangePercentage;
    }

    public Long getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(Long todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public Long getTodayBillCount() {
        return todayBillCount;
    }

    public void setTodayBillCount(Long todayBillCount) {
        this.todayBillCount = todayBillCount;
    }

    public long getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(Long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }
}
