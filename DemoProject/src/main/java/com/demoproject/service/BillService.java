package com.demoproject.service;


import com.demoproject.billqueue.BillRequest;
import com.demoproject.entity.Bill;
import com.demoproject.repository.BillRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
        bill.setCreatedAt(LocalDate.now());
        bill.setPorted(request.isPorted());
        bill.setDebt(request.isDebt());
        bill.setStoreId(request.getStoreId());
        bill.setStatus(true);
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
}
