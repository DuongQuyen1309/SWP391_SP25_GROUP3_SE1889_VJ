package com.demoproject.dto.request;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "Name cannot be empty.")
    @Pattern(regexp = "^[a-zA-ZÀ-Ỹà-ỹ\\s]+$", message = "Name must not contain numbers, special characters.")
    private String name;

    private Boolean gender;

    @PastOrPresent(message = "Date of Birth cannot be a future date.")
    private LocalDate dob;

    private String address;

    @NotBlank(message = "Phone number cannot be empty.")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be between 10 and 11 digits and contain only numbers.")
    private String phone;

    private Integer moneyState;
    private Long createdBy;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private LocalDate deletedAt;
    private Boolean isDelete = false;

    private Long storeId;
}
