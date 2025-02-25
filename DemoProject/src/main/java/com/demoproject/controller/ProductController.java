package com.demoproject.controller;

import com.demoproject.entity.Account;
import com.demoproject.entity.Product;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.ProductRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.ProductService;
import com.demoproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequestMapping("/product")
@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AccountService accountService;

    @GetMapping("/create")
    public String createProductForm(
            @CookieValue(value = "token", required = false) String token,
            Model model){
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        model.addAttribute("account", account.get());
        model.addAttribute("user", user.get());
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        return "product/createProduct";
    }


    @PostMapping("/create")
    public String handleFormSubmit(
            @RequestParam("name") String name,
            @RequestParam("price") double price,
            @RequestParam("description") String description,
            @CookieValue(value = "token", required = false) String token,
            Model model
    ) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        model.addAttribute("account", account.get());
        model.addAttribute("user", user.get());
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setIsDeleted(0);
        productRepository.save(product);
        return "redirect:/product/listProduct";
    }

    @GetMapping("/listProduct")
    public String showListProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @CookieValue(value = "token", required = false) String token,
            Model model) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        Optional<Users> optUser = userService.getUserProfile(account.getUserId());
        model.addAttribute("user", optUser.get());

        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        Page<Product> productPage = productService.getAllProductByPage(page, size, sortField, sortDirection);
        model.addAttribute("account", account);
        model.addAttribute("products", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");

//        List<Product> products = productService.getAllProductIsDeleted();
//        model.addAttribute("products", products);
        return "product/listProduct";
    }

    @PostMapping("/delete")
    public String deleteProduct(
            @RequestParam int id,
            @CookieValue(value = "token", required = false) String token
    ) {

        Product product = productService.getProductById(id);
        product.setIsDeleted(1);
        productRepository.save(product);
        return "redirect:/product/listProduct";
    }

    @GetMapping("/update")
    public String updateProductForm(
            @RequestParam int id, Model model,
            @CookieValue(value = "token", required = false) String token
    ) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        model.addAttribute("user", user.get());
        model.addAttribute("account", account.get());
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        Product product = productService.getProductById(id);
        model.addAttribute("products", product);
        return "product/updateproduct";
    }

    @PostMapping("/update")
    public String updateProduct(
            @RequestParam("id") String id,
            @RequestParam("name") String name,
            @RequestParam("price") double price,
            @RequestParam("description") String description,
            @CookieValue(value = "token", required = false) String token,
            Model model
    ) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        model.addAttribute("user", user.get());
        model.addAttribute("account", account.get());
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        int idProduct = Integer.parseInt(id);

        Product product = productService.getProductById(idProduct);
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        productRepository.save(product);
        return "redirect:/product/listProduct";
    }

    @GetMapping("/search")
    public String showSearchListProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam("searchKeyWord") String keyword,
            @RequestParam("searchBy") String searchBy,
            @CookieValue(value = "token", required = false) String token,
            Model model) {
        Page<Product> productPage = null;

        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        Optional<Users> optUser = userService.getUserProfile(account.getUserId());
        model.addAttribute("user", optUser.get());
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        model.addAttribute("account", account);
        if(searchBy.equalsIgnoreCase("name")){
            productPage = productService.getProductByKeyWordName(page, size, sortField, sortDirection, keyword);
        }else {
            productPage = productService.getProductByKeyWordDescription(page, size, sortField, sortDirection, keyword);
        }
        model.addAttribute("products", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");

        return "product/listProduct";
    }

}
