package com.demoproject.service;


import com.demoproject.billqueue.BillRequest;
import com.demoproject.entity.Bill;
import com.demoproject.repository.BillRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
}
