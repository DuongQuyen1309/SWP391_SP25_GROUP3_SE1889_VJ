package com.demoproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CustomerResponse {
    private String name;
    private Boolean gender;
    private String address;
    private LocalDate dob;
    private String phone;
    private Integer moneyState ;
    private String ctype;

}
