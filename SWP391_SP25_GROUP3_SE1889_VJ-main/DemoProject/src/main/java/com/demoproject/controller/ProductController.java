package com.demoproject.controller;


import com.demoproject.entity.Product;
import com.demoproject.repository.ProductRepository;
import com.demoproject.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private  final ProductRepository productRepository;

    @PostMapping("/form")
    public String handleFormSubmit(
            @RequestParam("name") String name,
            @RequestParam("price") double price,
            @RequestParam("description") String description
//            @RequestParam("image") MultipartFile image
    ) {
        // Tạo object Product
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setIsDeleted("0");

        // Xử lý và lưu đường dẫn hình ảnh
//        String filePath = "uploads/" + image.getOriginalFilename();
//        product.setImage(filePath);

        // Lưu vào database
        productRepository.save(product);

        return "redirect:/listproduct";
    }

    @GetMapping("/listproduct")
    public String listProducts(Model model) {
        // Lấy danh sách sản phẩm từ ProductService
        model.addAttribute("listproduct", productService.getAllProducts());
        return "listproduct";
    }

    @PostMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
//        try {
//            productService.deleteProduct(productId, "admin");
//        } catch (Exception e) {
//            return "error";
//        }
        Product product = productService.getProductById(id);
        product.setIsDeleted("1");
        productRepository.save(product);
        return "redirect:/listproduct";
    }

    @PostMapping("/update")
    public String updateProduct(
            @RequestParam("id") int id,
            @RequestParam("newName") String name,
            @RequestParam("newPrice") double price,
            @RequestParam("newDescription") String description
    ) {
//        try {
//            productService.deleteProduct(productId, "admin");
//        } catch (Exception e) {
//            return "error";
//        }
        Product product = productService.getProductById(id);
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        productRepository.save(product);
        return "redirect:/listproduct";
    }


}
