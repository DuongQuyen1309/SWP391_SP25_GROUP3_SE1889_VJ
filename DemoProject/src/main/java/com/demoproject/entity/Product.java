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
    int id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "price", nullable = false)
    double price;

    @Column(name = "description", nullable = false)
    String description;

//    @Column(name = "image", nullable = false)
//    String image;

    @Column(name = "createdBy", nullable = true)
    String createdBy;

//    @Column(name = "createdAt")  // Đổi tên cho đúng với DB
//    LocalDateTime createdAt;
//    @Column(name = "updatedBy", nullable = false)
//    String updatedBy;

    @Column(name = "updatedAt", nullable = true)
    LocalDateTime updatedAt;

//    @Column(name = "deletedBy", nullable = false)
//    String deletedBy;

    @Column(name = "deletedAt", nullable = true)
    LocalDateTime deletedAt;

    @Column(name = "isDelete", nullable = true)
    int isDeleted;
}
