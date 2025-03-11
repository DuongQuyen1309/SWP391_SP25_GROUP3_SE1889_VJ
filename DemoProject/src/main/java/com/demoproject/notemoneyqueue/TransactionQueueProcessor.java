package com.demoproject.notemoneyqueue;

import com.demoproject.service.CustomerBalanceService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class TransactionQueueProcessor {
    private final CustomerBalanceService customerBalanceService;
    private final BlockingQueue<TransactionRequest> transactionQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Chỉ 1 thread xử lý

    public TransactionQueueProcessor(CustomerBalanceService customerBalanceService) {
        this.customerBalanceService = customerBalanceService;
    }

    @PostConstruct
    public void startProcessing() {
        executorService.submit(() -> {
            while (true) {
                try {
                    TransactionRequest request = transactionQueue.take(); // Lấy yêu cầu từ hàng đợi
                    customerBalanceService.updateBalance(request); // Xử lý cập nhật số dư
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addTransaction(TransactionRequest request) {
        transactionQueue.offer(request);
    }
}
