package com.demoproject.service;

import com.demoproject.dto.ProductDTO;
import com.demoproject.entity.Product;
import com.demoproject.repository.ProductRepository;
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

    public Product getProductById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void deleteProduct(int productId, String deletedBy) throws Exception {
        Optional<Product> optionalProduct = productRepository.findById(productId);

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setIsDeleted(1); // Soft delete
            product.setDeletedAt(LocalDateTime.now());
//            product.setDeletedBy(deletedBy);

            productRepository.save(product);
        } else {
            throw new Exception("Product with ID " + productId + " not found");
        }
    }

    public Product updateProduct(ProductDTO productDTO) throws Exception {
        Optional<Product> optionalProduct = productRepository.findById(productDTO.getId());

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            product.setUpdatedAt(LocalDateTime.now());
//            product.setUpdatedBy(productDTO.getUpdatedBy());

            return productRepository.save(product);
        } else {
            throw new Exception("Product with ID " + productDTO.getId() + " not found");
        }
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


}
