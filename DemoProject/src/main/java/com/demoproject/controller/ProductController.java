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
            Model model){
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()){
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute("account", account.get());
            user.ifPresent(users -> model.addAttribute("user", users));
        }
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals(STAFF)){
            listHiddenPage.add(LISTSTAFF);
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
    )throws IOException {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()){
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute("account", account.get());
            if (user.isPresent()){
                model.addAttribute("user", user.get());
                productService.createProduct(user,name,description,price,imageFile);
            }
        }
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
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
            Model model) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        Optional<Users> optUser = userService.getUserProfile(account.getUserId());
        model.addAttribute("user", optUser.get());
        model.addAttribute("account", account);
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
            listHiddenPage.add("createProduct");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        Page<Product> productPage = productService.getAllProductByPage(optUser.get().getStoreId(),page, size, sortField, sortDirection);
        model.addAttribute("account", account);
        model.addAttribute("products", productPage.stream().toList());
        model.addAttribute("currentPage", page+1);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");

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
        if (account.isPresent()){
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute("account", account.get());
            user.ifPresent(users -> model.addAttribute("user", users));
        }
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
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
    )throws IOException {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()){
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute("account", account.get());
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                productService.updateProduct(user.get().getStoreId(), productID, name, description, price, imageFile);
            }
        }
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

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
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()){
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute("account", account.get());
            user.ifPresent(users -> model.addAttribute("user", users));
        }
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
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

    @GetMapping("/import")
    public String showImportPage(@CookieValue(value = "token", required = false) String token,
                                 Model model){
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()){
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute(ACCOUNT, account.get());
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                model.addAttribute("listProducts", productRepository.findByStoreId(user.get().getStoreId()));
                model.addAttribute("listZones", zoneRepository.findByStoreId(user.get().getStoreId()));
            }
        }
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals(STAFF)){
            listHiddenPage.add(LISTSTAFF);
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
    ){
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        if (account.isPresent()){
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            model.addAttribute(ACCOUNT, account.get());
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                productService.importProduct(user.get(), importProductDTO);
            }
        }
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals(STAFF)){
            listHiddenPage.add(LISTSTAFF);
        }
        model.addAttribute(LISTHIDDENPAGE, listHiddenPage);
        return "redirect:/product/listProduct";
    }

//    @GetMapping("/search-product")
//    @ResponseBody
//    public List<Product> searchProduct(@RequestParam String query) {
//        return productRepository.findByNameContainingIgnoreCase(query);
//    }
@GetMapping("/checkQuantity/{id}/{quantity}")
public ResponseEntity<Boolean> checkQuantity(@PathVariable Long id, @PathVariable int quantity) {
    Optional<Product> productOpt = productRepository.findById(id);
    boolean isAvailable = productOpt.isPresent() && productOpt.get().getQuantity() >= quantity;
    return ResponseEntity.ok(isAvailable); // ✅ Trả về ResponseEntity để Spring hiểu đây là JSON
}
}

