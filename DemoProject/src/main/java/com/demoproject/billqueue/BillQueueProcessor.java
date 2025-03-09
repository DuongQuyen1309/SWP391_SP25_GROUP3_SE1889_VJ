package com.demoproject.billqueue;

import com.demoproject.service.BillService;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BillQueueProcessor {

    private final BillService billService;
    private final BlockingQueue<BillRequest> billQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Xử lý từng hóa đơn một

    public BillQueueProcessor(BillService billService) {
        this.billService = billService;
    }

    @PostConstruct
    public void startProcessing() {
        executorService.submit(() -> {
            while (true) {
                try {
                    BillRequest request = billQueue.take(); // Lấy hóa đơn từ hàng đợi
                    billService.processBill(request); // Xử lý hóa đơn
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addBill(BillRequest request) {
        billQueue.offer(request); // Thêm hóa đơn vào hàng đợi
    }
}
