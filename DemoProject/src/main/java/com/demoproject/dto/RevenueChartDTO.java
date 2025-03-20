package com.demoproject.dto;

import java.time.LocalDate;

public class RevenueChartDTO {

    private LocalDate date;
    private Long revenue;
    private Long todayRevenue;
    private Long todayBillCount;
// Getter/Setter đầy đủ

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getRevenue() {
        return revenue;
    }

    public void setRevenue(Long revenue) {
        this.revenue = revenue;
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

    public RevenueChartDTO(LocalDate date, Long revenue) {
        this.date = date;
        this.revenue = revenue;
    }
}
