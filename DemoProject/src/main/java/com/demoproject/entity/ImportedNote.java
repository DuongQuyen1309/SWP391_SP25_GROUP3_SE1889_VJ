package com.demoproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Importednote")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportedNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "warehouse_name", nullable = false)
    String warehouseName;

    @Column(name = "note", nullable = true)
    String note;

    @Column(name = "totalcost", nullable = false)
    double totalCost;

    @Column(name = "product_data", nullable = false)
    String productData;

    @Column(name = "is_debt", nullable = false)
    boolean isDebt;

    @Column(name = "created_by", nullable = false)
    Long createdBy;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "store_id", nullable = false)
    Long storeId;

    @Column(name = "customer_id", nullable = false)
    Long customerId;
}
