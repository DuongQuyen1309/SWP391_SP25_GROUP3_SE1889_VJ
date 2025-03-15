package com.demoproject.entity;


import com.demoproject.dto.PackageTypeDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "price", nullable = false)
    double price;

    @Column(name = "description", nullable = false)
    String description;

    @Column(name = "url", nullable = true)
    String image;

    @Column(name = "created_by", nullable = false)
    Long createdBy;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = false)
    LocalDateTime deletedAt;

    @Column(name = "is_delete", nullable = false)
    int isDeleted;

    @Column(name = "store_id", nullable = false)
    Long storeId;

    @Column(name = "zone_id", nullable = false)
    Long zoneId;

    @Column(name = "quantity")
    int quantity;

    @Column(name = "package_type", columnDefinition = "nvarchar(MAX)")
    String packageTypeJson; // Lưu dưới dạng chuỗi JSON

    @Transient
    private List<PackageTypeDTO> packageType;

    public List<PackageTypeDTO> getPackageType() {
        if (packageType == null && packageTypeJson != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                packageType = objectMapper.readValue(packageTypeJson, new TypeReference<List<PackageTypeDTO>>() {});
            } catch (IOException e) {
                packageType = List.of();
            }
        }
        return packageType;
    }

    public void setPackageType(List<PackageTypeDTO> packageType) {
        this.packageType = packageType;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            this.packageTypeJson = objectMapper.writeValueAsString(packageType);
        } catch (IOException e) {
            this.packageTypeJson = "[]";
        }
    }

    @Transient
    private Long selectedPackageId;  // ✅ ID của loại đóng gói đang chọn

    @Transient
    private int selectedPackageSize; // ✅ Số kg tương ứng với package đang chọn

    public void setSelectedPackageSize(int selectedPackageSize) {
        this.selectedPackageSize = selectedPackageSize;
    }

    public void setSelectedPackageId(Long selectedPackageId) {
        this.selectedPackageId = selectedPackageId;
    }

    private int extractPackageSize(String packageTypeName) {
        try {
            return Integer.parseInt(packageTypeName.replaceAll("[^0-9]", "")); // ✅ Lấy số từ tên gói (ví dụ: "5kg" → 5)
        } catch (Exception e) {
            return 1; // ✅ Mặc định nếu không tìm thấy số
        }
    }
}

