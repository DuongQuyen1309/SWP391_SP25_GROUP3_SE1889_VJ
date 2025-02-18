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
public class UsersRequest {
    @NotBlank(message = "Name cannot be empty.")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name must not contain numbers or special characters.")
    private String name;

    private Boolean gender;

    @NotNull(message = "Date of Birth cannot be empty.")
    @PastOrPresent(message = "Date of Birth cannot be a future date.")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address cannot be empty.")
    private String address;

    @NotBlank(message = "Phone number cannot be empty.")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be between 10 and 11 digits and contain only numbers.")
    private String phone;


    private Long createdBy;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private LocalDate deletedAt;
    private Boolean isDelete = false;
    private String warehouseName;

}
