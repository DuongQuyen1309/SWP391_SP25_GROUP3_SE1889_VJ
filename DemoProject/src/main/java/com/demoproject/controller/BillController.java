package com.demoproject.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return productService.getProductsByNameAndCreatedBy(keyword,user.getId().toString());
    }

    @GetMapping("/searchCustomers")
    @ResponseBody
    public List<Customer> searchCustomers(@RequestParam String keyword, @CookieValue(value = "token", required = false) String token) {
        List<Long> relatedUserList = new ArrayList<>();
        relatedUserList = getUserId(token);
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        return customerService.searchCustomer(relatedUserList,keyword);
    }



    public List<Long> getUserId(@CookieValue(value = "token", required = false) String token){
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        String role= jwtUtils.extractRole(token);
        List<Long> relatedUserList = new ArrayList<>();
        if(role.equalsIgnoreCase("OWNER")){
            relatedUserList = userService.getStaffID(user.getId());
            relatedUserList.add(user.getId());

        }else if(role.equalsIgnoreCase("STAFF")){
            Long ownerId = userService.getOwnerID(user.getId());
            relatedUserList = userService.getStaffID(ownerId);
            relatedUserList.add(ownerId);

        }
        return relatedUserList;
    }
}
