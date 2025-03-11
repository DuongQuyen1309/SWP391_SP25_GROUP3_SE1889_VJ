package com.demoproject.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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

    @Column(name = "quantity", nullable = false)
    int quantity;
}
