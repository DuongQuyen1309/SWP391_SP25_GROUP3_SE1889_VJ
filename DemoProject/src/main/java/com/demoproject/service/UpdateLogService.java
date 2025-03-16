package com.demoproject.service;

import com.demoproject.entity.CustomerUpdateLog;
import com.demoproject.repository.CustomerUpdateLogRepository;
import com.demoproject.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UpdateLogService {
    @Autowired
    private final CustomerUpdateLogRepository updateLogRepo;
    public UpdateLogService(CustomerUpdateLogRepository updateLogRepo) {
        this.updateLogRepo = updateLogRepo;
    }

    public Page<CustomerUpdateLog> searchUpdateLogByAttribute(Long storeId,
                                                       String req_informationField, Long req_updatedBy,
                                                       String req_preValue, String req_followValue,
                                                       LocalDateTime updatedDateFrom, LocalDateTime updatedDateTo, String status, Pageable pageable){
        return updateLogRepo.getUpdateLogByAttribute(storeId,
                req_informationField, req_updatedBy, req_preValue, req_followValue, updatedDateFrom, updatedDateTo, status, pageable);
    }
    public Page<CustomerUpdateLog> searchAllUpdateLog(Long storeId, Pageable pageable){
        return updateLogRepo.getAllUpdateLog(storeId, pageable);
    }

    public CustomerUpdateLog getUpdateLogById(Long id){
        return updateLogRepo.findById(id).orElse(null);
    }

}
