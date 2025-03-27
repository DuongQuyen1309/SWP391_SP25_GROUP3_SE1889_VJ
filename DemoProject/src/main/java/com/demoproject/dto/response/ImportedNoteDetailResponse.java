package com.demoproject.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportedNoteDetailResponse {
    Long id;
    double totalCost,paidMoney,portedMoney,debtMoney;
    String customerName, customerPhone,customerAddress;
    LocalDate createdAt;
}
