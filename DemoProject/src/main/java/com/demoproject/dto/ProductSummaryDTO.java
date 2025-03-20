package com.demoproject.dto;

import com.demoproject.entity.Product;
import lombok.Data;
import java.util.List;

@Data
public class ProductSummaryDTO {
    private Long id;
    private String name;
    private int quantity;
    private double price;
    private List<PackageTypeDTO> packageType;
    private Long selectedPackage;
    private int selectedPackageSize;

    public ProductSummaryDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.quantity = product.getQuantity();
        this.price = product.getPrice();
        this.packageType = product.getPackageType();
        this.selectedPackage = product.getSelectedPackage();
        this.selectedPackageSize = product.getSelectedPackageSize();
    }
}

