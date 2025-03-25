package com.demoproject.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDataDTO {
    private String name;
    private String phone;
    private String address;
    private String dob;
    private String gender;
    private int moneyState; // Nợ cũ

    public CustomerDataDTO() {}

    public CustomerDataDTO(String name, String phone, String address, String dob, String gender, int moneyState) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.dob = dob;
        this.gender = gender;
        this.moneyState = moneyState;
    }

    // Getters và Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getMoneyState() { return moneyState; }
    public void setMoneyState(int moneyState) { this.moneyState = moneyState; }



}

