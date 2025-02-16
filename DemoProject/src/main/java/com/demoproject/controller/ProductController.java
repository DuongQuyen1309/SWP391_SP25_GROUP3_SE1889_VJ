package com.demoproject.controller;


import com.demoproject.entity.Product;
import com.demoproject.repository.ProductRepository;
import com.demoproject.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    ) {

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setIsDeleted(0);

        productRepository.save(product);

        return "redirect:/listproduct";
    }

    @GetMapping("/listproduct")
    public String showListProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            Model model) {
        Page<Product> productPage = productService.getAllProductByPage(page, size, sortField, sortDirection);

        model.addAttribute("products", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");

//        List<Product> products = productService.getAllProductIsDeleted();
//        model.addAttribute("products", products);
        return "listproduct";
    }

    @PostMapping("/delete")
    public String deleteProduct(@RequestParam int id) {

        Product product = productService.getProductById(id);
        product.setIsDeleted(1);
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

        Product product = productService.getProductById(id);
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        productRepository.save(product);
        return "redirect:/listproduct";
    }


}
