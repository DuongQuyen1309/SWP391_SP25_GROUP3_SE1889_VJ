package com.demoproject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    LocalDate createdAt;

    @Column(name = "store_id", nullable = false)
    Long storeId;

    @Column(name = "customer_id", nullable = false)
    Long customerId;

    @Column(name = "paid_money")
    double paidMoney;

    @Column(name = "debt_money")
    double debtMoney;

    @Column(name = "ported_money")
    double portedMoney;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    @JsonBackReference
    private Customer customer;
}
