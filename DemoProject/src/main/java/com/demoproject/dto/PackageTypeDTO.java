package com.demoproject.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageTypeDTO {
    private Long id;
    private String name; // Ví dụ: "1kg", "5kg"
}

