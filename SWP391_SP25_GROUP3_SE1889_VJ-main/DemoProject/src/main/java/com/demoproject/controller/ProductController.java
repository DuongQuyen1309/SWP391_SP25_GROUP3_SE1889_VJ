package com.demoproject.controller;

import com.demoproject.dto.ProductDTO;
import com.demoproject.entity.Product;
import com.demoproject.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller

public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public String showProductList() {
        return "list";
    }

    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }

    // Hiển thị form tạo sản phẩm
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("productDTO", new Product());
        return "createProduct";
    }

    // Xử lý submit form tạo sản phẩm
    @PostMapping("/create")
    public String createProduct(@ModelAttribute("productDTO") ProductDTO productDTO, Model model) {
        try {
            productService.createProduct(productDTO);
            model.addAttribute("message", "Product created successfully!");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "createProduct";
    }

}
