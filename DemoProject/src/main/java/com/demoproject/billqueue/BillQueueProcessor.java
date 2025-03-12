package com.demoproject.billqueue;

import com.demoproject.entity.Bill;
import com.demoproject.entity.Product;
import com.demoproject.productqueue.ProductQueueProcessor;
import com.demoproject.repository.BillRepository;
import com.demoproject.service.BillService;
import com.demoproject.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class BillQueueProcessor {

    private final BillService billService;
    private final BlockingQueue<BillRequest> billQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Xử lý từng hóa đơn một
    private final ProductService productService;
    private final ProductQueueProcessor productQueueProcessor;
    private final ObjectMapper objectMapper;
    private final Map<String, Long> trackingMap = new ConcurrentHashMap<>(); // Lưu mapping trackingId -> billId
    private final String checkProduct=null;
    private final BillRepository billRepository;

    public BillQueueProcessor(BillService billService, ProductService productService, ProductQueueProcessor productQueueProcessor, ObjectMapper objectMapper, BillRepository billRepository) {
        this.billService = billService;
        this.productService = productService;
        this.productQueueProcessor = productQueueProcessor;
        this.objectMapper = objectMapper;
        this.billRepository = billRepository;
    }

    public void addBill(String trackingId,BillRequest request) {
        System.out.println("trong queue"+trackingId);

        request.setTrackingId(trackingId);
        System.out.println("BIllid: "+request.getBillId());
        billQueue.offer(request); // Thêm hóa đơn vào hàng đợi

    }

    @PostConstruct
    public void startProcessing() {
        executorService.submit(() -> {
            while (true) {
                try {
                    BillRequest request = billQueue.take(); // Lấy hóa đơn từ hàng đợi
                    System.out.println("🔍 Xử lý hóa đơn: " + request.getBillId());

                    // 🔹 Kiểm tra số lượng gạo trong kho trước khi xử lý hóa đơn
                    if (!productService.checkStockBeforeProcessing(request.getProductData())) {
                        System.out.println("❌ Không đủ hàng trong kho");
                        continue; // Dừng xử lý hóa đơn này
                    }

                    if(request.getDiscount() > 10){
                        request.setStatus(true);
                    }

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
                    bill.setNote(request.getNote());

                    System.out.println("✅ Xử lý hóa đơn thành công");

                    // ✅ Nếu đủ hàng, lưu hóa đơn vào database
                    billRepository.save(bill);

                    // ✅ Cập nhật trackingId -> billId trong mapping
                    trackingMap.put(request.getTrackingId(), bill.getId());


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


    public Long getBillIdByTrackingId(String trackingId) {
        System.out.println(trackingMap.get(trackingId));
        return trackingMap.get(trackingId);
    }



}
