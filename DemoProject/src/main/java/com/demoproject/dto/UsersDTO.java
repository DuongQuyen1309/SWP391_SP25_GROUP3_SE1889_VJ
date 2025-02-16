package com.demoproject.dto;


import jakarta.validation.constraints.*;
import java.time.LocalDate;


public class UsersDTO {

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 3, max = 50, message = "Tên phải có từ 3 đến 50 ký tự")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải trước hiện tại")
    private LocalDate dateOfBirth;

    @NotNull(message = "Giới tính không được để trống")
    private Boolean gender;

    // Getters và Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Boolean getGender() { return gender; }
    public void setGender(Boolean gender) { this.gender = gender; }
}

