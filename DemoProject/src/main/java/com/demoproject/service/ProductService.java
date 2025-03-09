package com.demoproject.service;

import com.demoproject.dto.ProductDTO;
import com.demoproject.entity.Product;
import com.demoproject.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public void createProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCreatedBy(productDTO.getCreatedBy());
//        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setIsDeleted(0);

        productRepository.save(product);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByNameAndCreatedBy(String productName, Long storeId) {
        return productRepository.findByNameContainingAndStoreId(productName, storeId);
    }




    public List<Product> getAllProductIsDeleted() {
        return productRepository.getProductByIsDeleted(0);
    }

    public Page<Product> getAllProductByPage(int page, int size, String sortFiled, String sortDirection){
        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortFiled).ascending() :
                Sort.by(sortFiled).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable);
    }

    public Page<Product> getProductByKeyWordName(int page, int size, String sortFiled, String sortDirection, String keyword){
        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortFiled).ascending() :
                Sort.by(sortFiled).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByNameContaining(keyword,pageable);
    }

    public Page<Product> getProductByKeyWordDescription(int page, int size, String sortFiled, String sortDirection, String keyword){
        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortFiled).ascending() :
                Sort.by(sortFiled).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByDescriptionContaining(keyword,pageable);
    }

    public String getProductNameById(long productId){
        return productRepository.findById(productId)
                .map(Product::getName)
                .orElse("Product Not Found");

    }

    public List<Product> getProductsByZoneId(Long zoneId) {
        return productRepository.findByZoneId(zoneId);
    }
    public List<Product> searchProductsByZoneIdAndName(Long zoneId, String name) {
        return productRepository.findByZoneIdAndNameContaining(zoneId, name);
    }

    public List<Product> searchProductsByZoneIdAndPrice(Long zoneId, Double minPrice, Double maxPrice) {
        return productRepository.findByZoneIdAndPriceBetween(zoneId, minPrice, maxPrice);
    }

    @Transactional
    public void updateStockAfterBill(String productDataJson) {
        try {
            // ✅ Chuyển đổi JSON thành danh sách `ProductOrder`
            List<Product> products = objectMapper.readValue(productDataJson, new TypeReference<List<Product>>() {});

            for (Product productOrder : products) {
                Product product = productRepository.findById(productOrder.getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + productOrder.getId()));

                if (product.getQuantity() < productOrder.getQuantity()) {
                    throw new RuntimeException("Không đủ hàng trong kho: " + productOrder.getId());
                }

                product.setQuantity(product.getQuantity() - productOrder.getQuantity());
                productRepository.save(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật số lượng sản phẩm: " + e.getMessage());
        }
    }

}
