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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    String id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "price", nullable = false)
    double price;

    @Column(name = "description", nullable = false)
    String description;

    @Column(name = "image", nullable = false)
    String image;

    @Column(name = "createdBy", nullable = false)
    String createdBy;

    @Column(name = "createdAt", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updatedBy", nullable = false)
    String updatedBy;

    @Column(name = "updatedAt", nullable = false)
    LocalDateTime updatedAt;

    @Column(name = "deletedBy", nullable = false)
    String deletedBy;

    @Column(name = "deletedAt", nullable = false)
    LocalDateTime deletedAt;

    @Column(name = "isDeleted", nullable = false)
    String isDeleted;
}
