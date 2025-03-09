package com.demoproject.productqueue;

import com.demoproject.entity.Product;
import com.demoproject.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ProductQueueProcessor {
    private final ProductService productService;
    private final BlockingQueue<String> productQueue = new LinkedBlockingQueue<>(); // ✅ Đúng: Lưu JSON dưới dạng String

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Xử lý tuần tự

    public ProductQueueProcessor(ProductService productService) {
        this.productService = productService;
    }

    @PostConstruct
    public void startProcessing() {
        executorService.submit(() -> {
            while (true) {
                try {
                    String productDataJson = productQueue.take(); // ✅ Lấy JSON từ hàng đợi
                    productService.updateStockAfterBill(productDataJson); // ✅ Gửi JSON đến service
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addProductsToQueue(List<Product> productList) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String productDataJson = objectMapper.writeValueAsString(productList); // ✅ Chuyển `List<Product>` thành JSON
            productQueue.offer(productDataJson); // ✅ Đưa JSON vào hàng đợi
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

