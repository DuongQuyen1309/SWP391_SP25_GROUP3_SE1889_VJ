package com.demoproject.dto;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDTO {
    int id;
    String name;
    double price;
    String description;
    String image;
    String createdBy;
    String updatedBy;

}
