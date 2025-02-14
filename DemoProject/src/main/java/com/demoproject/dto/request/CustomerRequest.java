package com.demoproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CustomerRequest {
    private Long id;
    private String name;
    private Boolean gender;
    private String address;
    private String phone;
    private Integer moneyState ;
}
