package com.demoproject.controller;

import com.demoproject.dto.request.CustomerRequest;
import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import com.demoproject.entity.Product;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.AccountRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.CustomerService;
import com.demoproject.service.ProductService;
import com.demoproject.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/bill")
public class BillController {
    @Autowired
    private final UserService userService;
    private final AccountService accountService;
    private final ProductService productService;
    @Autowired
    private AccountRepository accountRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private CustomerService customerService;

    public BillController(AccountService accountService,UserService userService,ProductService productService) {
        this.accountService = accountService;
        this.userService = userService;
        this.productService = productService;
    }

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/listBill")
    public String listBill(@CookieValue(value = "token", required = false) String token,
                           Model model) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        model.addAttribute("account", optAccount.get());
        model.addAttribute("user", user);
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        return "bill/listBill";
    }

    @GetMapping("/billDetail")
    public String billDetail( Model model) {

        return "bill/billDetail";
    }

    @GetMapping("/createBill")
    public String createBill(@CookieValue(value = "token", required = false) String token,
                             Model model,
                             @RequestParam(required = false) String productName) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
         model.addAttribute("account", optAccount.get());
        model.addAttribute("user", user);
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        return "bill/createBill";
    }

    // Thêm API tìm kiếm sản phẩm
    @GetMapping("/searchProducts")
    @ResponseBody
    public List<Product> searchProducts(@RequestParam String keyword,@CookieValue(value = "token", required = false) String token) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        return productService.getProductsByNameAndCreatedBy(keyword,user.getStoreId());
    }

    @GetMapping("/searchCustomers")
    @ResponseBody
    public List<Customer> searchCustomers(@RequestParam String keyword, @CookieValue(value = "token", required = false) String token) {

        Account account = accountService.getAccountFromToken(token).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        List<Customer> customers= customerService.searchCustomer(user.getStoreId(),keyword);

        return customers;
    }


    @PostMapping("/addCustomer")
    @ResponseBody
    public Map<String, Object> addCustomer(@RequestBody CustomerRequest customerRequest,
                                           @CookieValue(value = "token", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        String phone = customerRequest.getPhone().trim();
        customerRequest.setPhone(phone.trim());
        customerRequest.setMoneyState(0);
        customerRequest.setCreatedBy(user.getId());
        customerRequest.setCreatedAt(LocalDate.now());
        customerRequest.setStoreId(user.getStoreId());
        try {
            customerService.createCustomer(customerRequest);
            response.put("success", true);
            response.put("message", "Thêm khách hàng thành công!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi thêm khách hàng: " + e.getMessage());
        }
        return response;
    }

}
