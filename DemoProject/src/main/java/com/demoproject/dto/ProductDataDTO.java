package com.demoproject.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDataDTO {
    private Long id;
    private String name;
    private int quantity;
    private double price; // Changed from unitPrice to price
    private double total;
    private String zone;
    private String note;
    private Long packageTypeId; // ✅ ID của loại đóng gói (ví dụ: 1kg, 5kg)
    private String packageTypeName; // ✅ Tên của loại đóng gói (ví dụ: "1kg", "5kg")
    private List<PackageTypeDTO> packageType;
    private Long selectedPackage;
    private int selectedPackageSize;

    // Getters and Setters


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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() { // Changed from getUnitPrice
        return price;
    }

    public void setPrice(double price) { // Changed from setUnitPrice
        this.price = price;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getPackageTypeId() {
        return packageTypeId;
    }

    public void setPackageTypeId(Long packageTypeId) {
        this.packageTypeId = packageTypeId;
    }



    public String getPackageTypeName() {
        // ✅ Nếu `packageTypeName` bị null, tự động lấy từ `selectedPackage`
        if (packageTypeName == null && packageType != null) {
            packageTypeName = packageType.stream()
                    .filter(pkg -> pkg.getId().equals(selectedPackage))
                    .map(PackageTypeDTO::getName)
                    .findFirst()
                    .orElse(null);
        }
        return packageTypeName;
    }

    public void setPackageTypeName(String packageTypeName) {
        this.packageTypeName = packageTypeName;
    }

    public List<PackageTypeDTO> getPackageType() {
        return packageType;
    }

    public void setPackageType(List<PackageTypeDTO> packageType) {
        this.packageType = packageType;
    }

    public Long getSelectedPackage() {
        return selectedPackage;
    }

    public void setSelectedPackage(Long selectedPackage) {
        this.selectedPackage = selectedPackage;
    }

    public int getSelectedPackageSize() {
        return selectedPackageSize;
    }

    public void setSelectedPackageSize(int selectedPackageSize) {
        this.selectedPackageSize = selectedPackageSize;
    }
}