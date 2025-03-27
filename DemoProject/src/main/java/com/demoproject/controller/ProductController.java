package com.demoproject.controller;

import com.demoproject.dto.ImportProductDTO;
import com.demoproject.entity.Account;
import com.demoproject.entity.Product;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.ProductRepository;
import com.demoproject.repository.ZoneRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.ProductService;
import com.demoproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final ZoneRepository zoneRepository;
    private final String ACCOUNT = "account", STAFF = "STAFF", LISTSTAFF = "listStaff", LISTHIDDENPAGE = "listHiddenPage";

    @GetMapping("/create")
    public String createProductForm(
            @CookieValue(value = "token", required = false) String token,
            Model model) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()) {
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute("account", account.get());
            user.ifPresent(users -> model.addAttribute("user", users));
        }
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if (role.equals(STAFF)) {
            listHiddenPage.add("Store");
            listHiddenPage.add(LISTSTAFF);
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute(LISTHIDDENPAGE, listHiddenPage);
        return "product/createProduct";
    }


    @PostMapping("/create")
    public String handleFormSubmit(
            @RequestParam("name") String name,
            @RequestParam("price") double price,
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile imageFile,
            @CookieValue(value = "token", required = false) String token,
            Model model
    ) throws IOException {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()) {
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute("account", account.get());
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                productService.createProduct(user, name, description, price, imageFile);
            }
        }
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if (role.equals("STAFF")) {
            listHiddenPage.add("Store");
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        return "redirect:/product/listProduct";
    }

    @GetMapping("/listProduct")
    public String showListProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @CookieValue(value = "token", required = false) String token,
            @RequestParam(defaultValue = "") String searchKeyWord,
            @RequestParam(defaultValue = "") String searchBy,
            Model model) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        Optional<Users> optUser = userService.getUserProfile(account.getUserId());
        model.addAttribute("user", optUser.get());
        model.addAttribute("account", account);
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
            listHiddenPage.add("createProduct");
            listHiddenPage.add("Store");
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        Page<Product> productPage = productService.getAllProductByPage(searchKeyWord, searchBy, optUser.get().getStoreId(), page, size, sortField, sortDirection);
        model.addAttribute("listHiddenPage", listHiddenPage);
        model.addAttribute("account", account);
        model.addAttribute("products", productPage.stream().toList());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);

        String reverseSortDirection = sortDirection.equals("asc") ? "desc" : "asc";
        model.addAttribute("reverseSortDirection", reverseSortDirection);

        model.addAttribute("searchKeyWord", searchKeyWord);
        model.addAttribute("searchBy", searchBy);
        return "product/listProduct";
    }


    @PostMapping("/delete")
    public String deleteProduct(
            @RequestParam Long id,
            @CookieValue(value = "token", required = false) String token
    ) {

        productService.deleteProduct(id);
        return "redirect:/product/listProduct";
    }

    @GetMapping("/update")
    public String updateProductForm(
            @RequestParam Long id, Model model,
            @CookieValue(value = "token", required = false) String token
    ) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()) {
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute("account", account.get());
            user.ifPresent(users -> model.addAttribute("user", users));
        }
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Store");
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        Product product = productService.getProductById(id);
        model.addAttribute("products", product);
        return "product/updateProduct";
    }

    @PostMapping("/update")
    public String updateProduct(
            @RequestParam("id") String productID,
            @RequestParam("name") String name,
            @RequestParam("price") double price,
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile imageFile,
            @CookieValue(value = "token", required = false) String token,
            Model model
    ) throws IOException {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()) {
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute("account", account.get());
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                productService.updateProduct(user.get().getStoreId(), productID, name, description, price, imageFile);
            }
        }
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if (role.equals("STAFF")) {
            listHiddenPage.add("Store");
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        return "redirect:/product/listProduct";
    }



    @GetMapping("/import")
    public String showImportPage(@CookieValue(value = "token", required = false) String token,
                                 Model model) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()) {
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute(ACCOUNT, account.get());
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                model.addAttribute("listProducts", productRepository.findByStoreId(user.get().getStoreId()));
                model.addAttribute("listZones", zoneRepository.findByStoreId(user.get().getStoreId()));
            }
        }
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if (role.equals(STAFF)) {
            listHiddenPage.add(LISTSTAFF);
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
            listHiddenPage.add("Store");
        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute(LISTHIDDENPAGE, listHiddenPage);
        model.addAttribute("listHiddenPage", listHiddenPage);

        return "product/importProduct";
    }

    @PostMapping("/import")
    public String importProduct(
            @RequestBody ImportProductDTO importProductDTO,
            @CookieValue(value = "token", required = false) String token,
            Model model
    ) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()) {
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute(ACCOUNT, account.get());
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                productService.importProduct(user.get(), importProductDTO);
            }
        }
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if (role.equals(STAFF)) {
            listHiddenPage.add(LISTSTAFF);
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
            listHiddenPage.add("Store");
        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute(LISTHIDDENPAGE, listHiddenPage);
        return "redirect:/product/listProduct";
    }

    @GetMapping("/checkQuantity/{id}/{quantity}")
    public ResponseEntity<Boolean> checkQuantity(@PathVariable Long id, @PathVariable int quantity) {
        Optional<Product> productOpt = productRepository.findById(id);
        boolean isAvailable = productOpt.isPresent() && productOpt.get().getQuantity() >= quantity;
        return ResponseEntity.ok(isAvailable); // ✅ Trả về ResponseEntity để Spring hiểu đây là JSON
    }

    //Ham search product cua importedProduct.html (importedProduct.css call)
    @GetMapping("/searchProducts")
    @ResponseBody
    public List<Product> searchProducts(
            @RequestParam String keyword,
            @CookieValue(value = "token", required = false) String token
    ) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        List<Product> products = productService.getProductsByNameAndStoreId(keyword, user.getStoreId());
        for(Product product : products){
            System.out.println(product.getPackageType());
        }
        return products;
    }
}

