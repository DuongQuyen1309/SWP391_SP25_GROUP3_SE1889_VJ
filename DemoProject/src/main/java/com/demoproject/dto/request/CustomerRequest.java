package com.demoproject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CustomerRequest {
    private String name;
    private Boolean gender;
    private LocalDate dob;
    private String address;
    private String phone;
    private Integer moneyState ;
}
