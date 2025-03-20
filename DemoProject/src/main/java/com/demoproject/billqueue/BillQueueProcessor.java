package com.demoproject.billqueue;

import com.demoproject.dto.ProductSummaryDTO;
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
import java.util.stream.Collectors;

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

                    List<Product> products = objectMapper.readValue(request.getProductData(), new TypeReference<List<Product>>() {});
                    for (Product product : products) {
                        System.out.println("📌 BillQueueProcessor - Product ID: " + product.getId() +
                                ", selectedPackage: " + product.getSelectedPackage());
                    }

                    // ✅ Cập nhật số lượng sản phẩm theo kg trước khi lưu vào bill
                    for (Product product : products) {
                        int totalKg = product.getQuantity() * product.getSelectedPackageSize();
                        product.setQuantity(totalKg); // ✅ Chuyển số lượng về tổng số kg thực tế

                    }


                    // ✅ Chỉ lưu các thông tin cần thiết của sản phẩm vào bill
                    List<ProductSummaryDTO> filteredProducts = products.stream()
                            .map(ProductSummaryDTO::new)
                            .collect(Collectors.toList());

                    String updatedProductData = objectMapper.writeValueAsString(filteredProducts); // ✅ Chỉ lưu các thông tin cần thiết

                    System.out.println("📌 BillQueueProcessor - updatedProductData: " + updatedProductData);

                    Bill bill = new Bill();
                    bill.setTotalMoney(request.getTotalMoney());
                    bill.setPaidMoney(request.getPaidMoney());
                    bill.setDebtMoney(request.getDebtMoney());
                    bill.setProductData(updatedProductData);
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

                    // ✅ Kiểm tra productData trước khi add vào queue
                    System.out.println("📌 Kiểm tra productData trước khi gửi vào hàng đợi: " + bill.getProductData());



                    // ✅ Đưa sản phẩm vào hàng đợi để cập nhật kho
                    productQueueProcessor.addProductsToQueue(
                            objectMapper.readValue(bill.getProductData(), new TypeReference<List<Product>>() {})
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
