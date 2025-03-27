package com.demoproject.service;


import com.demoproject.billqueue.BillRequest;
import com.demoproject.entity.Bill;
import com.demoproject.repository.BillRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BillService {

    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    @Transactional
    public void processBill(BillRequest request) {
        Bill bill = new Bill();
        bill.setTotalMoney(request.getTotalMoney());
        bill.setPaidMoney(request.getPaidMoney());
        bill.setDebtMoney(request.getDebtMoney());
        bill.setProductData(request.getProductData());
        bill.setCustomerData(request.getCustomerData());
        bill.setCreatedBy(request.getCreatedBy());
        bill.setCreatedAt(LocalDateTime.now());
        bill.setPorted(request.isPorted());
        bill.setDebt(request.isDebt());
        bill.setStoreId(request.getStoreId());
        bill.setStatus(request.isStatus());
        bill.setNote(request.getNote());
        bill.setDiscount(request.getDiscount());
        bill.setPortedMoney(request.getPortedMoney());
        // Lưu vào database
        billRepository.save(bill);
    }
    public Page<Bill> getAllBillsByStoreId(Pageable pageable, long storeId) {
        return this.billRepository.findAllByStoreId(storeId, pageable);
    }
    public Page<Bill> getBillsByStoreIdAndBillId(Long id,Long storeId,Pageable pageable) {
        return  this.billRepository.findByBillIdAndStoreId(id, storeId, pageable);
    }
    public Page<Bill> getBillsByStoreIdAndCustomerName(String name,Long storeId,Pageable pageable) {
        return  this.billRepository.findByNameAndStoreId(name, storeId, pageable);
    }

    public Optional<Bill> getBillByIdAndStoreId(Long billId, Long storeId) {
        return billRepository.findByIdAndStoreId(billId, storeId);
    }
    public Page<Bill> getBillsByTotalMoneyRange(Double minValue, Double maxValue, Long storeId, Pageable pageable) {
        return billRepository.findByTotalMoneyBetweenAndStoreId(minValue, maxValue, storeId, pageable);


    }
    public Page<Bill> getBillsByDateRange(LocalDate startDate, LocalDate endDate, Long storeId, Pageable pageable) {
        return billRepository.findByStoreIdAndCreatedAtBetween(storeId, startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(), pageable);
    }

    public Page<Bill> getBillsWithFilters(Long idFrom, Long idTo, String customerName,
                                          LocalDate startDate, LocalDate endDate,
                                          Double totalMoneyFrom, Double totalMoneyTo,
                                          Double paidMoneyFrom, Double paidMoneyTo,
                                          Double debtMoneyFrom, Double debtMoneyTo,
                                          Boolean status, String note,
                                          Long storeId, Pageable pageable) {

        Specification<Bill> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (idFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("id"), idFrom));
            if (idTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("id"), idTo));
            if (customerName != null && !customerName.isEmpty())
                predicates.add(cb.like(
                        cb.function("JSON_VALUE", String.class, root.get("customerData"), cb.literal("$.name")),
                        "%" + customerName + "%"
                ));
            if (startDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
            if (endDate != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate.plusDays(1).atStartOfDay()));
            if (totalMoneyFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalMoney"), totalMoneyFrom));
            if (totalMoneyTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("totalMoney"), totalMoneyTo));
            if (paidMoneyFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("paidMoney"), paidMoneyFrom));
            if (paidMoneyTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("paidMoney"), paidMoneyTo));
            if (debtMoneyFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("debtMoney"), debtMoneyFrom));
            if (debtMoneyTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("debtMoney"), debtMoneyTo));
            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));
            if (note != null && !note.isEmpty())
                predicates.add(cb.like(root.get("note"), "%" + note + "%"));

            predicates.add(cb.equal(root.get("storeId"), storeId));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return billRepository.findAll(spec, pageable);
    }
}
