package com.demoproject.service;

import com.demoproject.dto.response.ImportedNoteDetailResponse;
import com.demoproject.entity.ImportedNote;
import com.demoproject.repository.ImportedNoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class ImportedNoteService {

    private final ImportedNoteRepository importedNoteRepository;
    private final ObjectMapper objectMapper;

    public Page<ImportedNote> getImportedNotes(Long storeID, int page, int size, String sortFiled, String sortDirection){
        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortFiled).ascending() :
                Sort.by(sortFiled).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return importedNoteRepository.findByStoreId(storeID,pageable);
    }

    public ImportedNoteDetailResponse getImportedNoteById(Long id) {
        ImportedNote im =  importedNoteRepository.findImportedNoteWithCustomer(id).orElse(null);
        if (im != null) System.out.println(im.getCustomer().getName());

        return ImportedNoteDetailResponse.builder()
                .id(im.getId())
                .customerName(im.getCustomer().getName())
                .customerAddress(im.getCustomer().getAddress())
                .customerPhone(im.getCustomer().getPhone())
                .debtMoney(im.getDebtMoney())
                .paidMoney(im.getPaidMoney())
                .portedMoney(im.getPortedMoney())
                .createdAt(im.getCreatedAt())
                .totalCost(im.getTotalCost())
                .productData(im.getProductData())
                .build();
    }


    public Page<ImportedNote> findImportedNotesWithFilters(
            int page, int size, String sortField, String sortDirection,
            String idFrom, String idTo,
            LocalDate dateFrom, LocalDate dateTo,
            String moneyFrom, String moneyTo,
            String supplier) {
        double minMoney = moneyFrom != null ? Double.parseDouble(moneyFrom) : 0;
        double maxMoney = moneyTo != null ? Double.parseDouble(moneyTo) : Double.MAX_VALUE;
        long minId = idFrom != null ? Long.parseLong(idFrom) : 0;
        long maxId = idTo != null ? Long.parseLong(idTo) : Long.MAX_VALUE;

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return importedNoteRepository.findByFilters(minId, maxId, dateFrom, dateTo, minMoney, maxMoney, supplier, pageable);
    }

}
