package com.demoproject.billqueue;

import com.demoproject.entity.Bill;
import com.demoproject.entity.Product;
import com.demoproject.productqueue.ProductQueueProcessor;
import com.demoproject.service.BillService;
import com.demoproject.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BillQueueProcessor {

    private final BillService billService;
    private final BlockingQueue<BillRequest> billQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Xử lý từng hóa đơn một
    private final ProductService productService;
    private final ProductQueueProcessor productQueueProcessor;
    private final ObjectMapper objectMapper;

    public BillQueueProcessor(BillService billService, ProductService productService, ProductQueueProcessor productQueueProcessor, ObjectMapper objectMapper) {
        this.billService = billService;
        this.productService = productService;
        this.productQueueProcessor = productQueueProcessor;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void startProcessing() {
        executorService.submit(() -> {
            while (true) {
                try {
                    BillRequest request = billQueue.take(); // Lấy hóa đơn từ hàng đợi

                    // 🔹 Kiểm tra số lượng gạo trong kho trước khi xử lý hóa đơn
                    if (!productService.checkStockBeforeProcessing(request.getProductData())) {
                        System.out.println("❌ Không đủ hàng trong kho");



                        request.setStatus(false); // 🚨 Đánh dấu hóa đơn thất bại
                        request.setNote("❌ Không đủ hàng trong kho"); // Ghi chú lỗi

                        billService.processBill(request); // ✅ Lưu vào database
                        continue; // Dừng xử lý hóa đơn này
                    }

                    request.setStatus(true);
                    request.setNote("✅ Đã xử lý hóa đơn thành công");
                    // ✅ Nếu đủ hàng, lưu hóa đơn vào database
                    billService.processBill(request);

                    // ✅ Đưa sản phẩm vào hàng đợi để cập nhật kho
                    productQueueProcessor.addProductsToQueue(
                            objectMapper.readValue(request.getProductData(), new TypeReference<List<Product>>() {})
                    );

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
