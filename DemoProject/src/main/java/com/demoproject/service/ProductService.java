package com.demoproject.service;

import com.demoproject.dto.ImportProductDTO;
import com.demoproject.dto.ProductDataDTO;
import com.demoproject.entity.ImportedNote;
import com.demoproject.entity.Product;
import com.demoproject.entity.Users;
import com.demoproject.repository.ImportedNoteRepository;
import com.demoproject.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ImportedNoteRepository importedNoteRepository;

    public void createProduct(Optional<Users> users, String name, String description, double price, MultipartFile imageFile) throws IOException {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        if (users.isPresent()) {
            if (!imageFile.isEmpty()) {
                String originalFileName = imageFile.getOriginalFilename();
                String fileExtension = originalFileName.contains(".") ?
                        originalFileName.substring(originalFileName.lastIndexOf(".")) : ".png"; // Mặc định PNG nếu không có phần mở rộng

                // Tạo tên file ngắn gọn
                String fileName = UUID.randomUUID().toString().substring(0, 8) + fileExtension;
                String uploadDir = "uploads/images/";

                // Tạo thư mục nếu chưa có
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Lưu file vào thư mục
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Cập nhật đường dẫn ảnh trong Product
                String imageUrl = "/images/" + fileName;
                product.setImage(imageUrl);
            }

            product.setCreatedBy(users.get().getId());
            product.setStoreId(users.get().getStoreId());
        }
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setIsDeleted(0);
        product.setQuantity(0);
        productRepository.save(product);
    }

    public void updateProduct(Long storeID,String productID,String name, String description, double price, MultipartFile imageFile) throws IOException {
        Optional<Product> product = productRepository.findByIdAndStoreId(Long.parseLong(productID),storeID);
        if (product.isPresent()) {
            if (!imageFile.isEmpty()) {
                String originalFileName = imageFile.getOriginalFilename();
                String fileExtension = originalFileName.contains(".") ?
                        originalFileName.substring(originalFileName.lastIndexOf(".")) : ".png"; // Mặc định PNG nếu không có phần mở rộng

                // Tạo tên file ngắn gọn
                String fileName = UUID.randomUUID().toString().substring(0, 8) + fileExtension;
                String uploadDir = "uploads/images/";

                // Tạo thư mục nếu chưa có
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Lưu file vào thư mục
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Cập nhật đường dẫn ảnh trong Product
                String imageUrl = "/images/" + fileName;
                product.get().setImage(imageUrl);
            }
            product.get().setName(name);
            product.get().setDescription(description);
            product.get().setPrice(price);
            product.get().setUpdatedAt(LocalDateTime.now());
            productRepository.save(product.get());
        }else throw new RuntimeException("Bạn không có quyền chỉnh sửa sản phẩm này!!!");
    }

    public void deleteProduct(long productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()) {
            product.get().setDeletedAt(LocalDateTime.now());
            product.get().setIsDeleted(1);
            productRepository.save(product.get());
        }
    }

    public void importProduct(Users users, ImportProductDTO importProductDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<ProductDataDTO> dataDTOList = importProductDTO.getProductData();

        String productDataJson = "";
        try {
            productDataJson = objectMapper.writeValueAsString(dataDTOList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        double totalPrice = 0;
        for (ProductDataDTO productDataDTO : importProductDTO.getProductData()) {
            // Kiểm tra id trước khi tìm product thông qua id
            if(productDataDTO.getId()!=null){
                Optional<Product> product = productRepository.findById(productDataDTO.getId());
                if (product.isPresent()) {
                    product.get().setQuantity(product.get().getQuantity() + productDataDTO.getQuantity());
                    product.get().setZoneId(Long.parseLong(productDataDTO.getZone()));
                    productRepository.save(product.get());
                }
            }
            totalPrice += productDataDTO.getSubtotal();
        }

        //Xử lí importeDate sang dạng localdatetime
        LocalDate lcdate = null;
        if (importProductDTO.getImportDate().equals("")){
            lcdate = LocalDate.now();
        }else lcdate = LocalDate.parse(importProductDTO.getImportDate());

        ImportedNote importedNote = new ImportedNote();
        importedNote.setTotalCost(totalPrice);
        importedNote.setProductData(productDataJson);
        importedNote.setCreatedAt(lcdate.atStartOfDay());
        importedNote.setCreatedBy(users.getId());
        importedNote.setStoreId(users.getStoreId());
        importedNote.setWarehouseName(users.getName());
        importedNoteRepository.save(importedNote);
    }


    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getProductsByNameAndCreatedBy(String productName, Long storeId) {
        return productRepository.findByNameContainingAndStoreId(productName, storeId);
    }

    public Page<Product> getAllProductByPage(Long storeID,int page, int size, String sortFiled, String sortDirection){
        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortFiled).ascending() :
                Sort.by(sortFiled).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByStoreId(storeID,pageable);
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


}
