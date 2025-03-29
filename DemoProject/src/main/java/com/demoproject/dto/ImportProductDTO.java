package com.demoproject.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportProductDTO {
    String importDate, customerPhone;
    double paidMoney, debtMoney, portedMoney;
    List<ProductDTO> productData;
}
