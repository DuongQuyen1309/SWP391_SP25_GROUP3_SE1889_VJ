package com.demoproject.controller;

import com.demoproject.dto.StoreDTO;
import com.demoproject.entity.Account;
import com.demoproject.entity.Store;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.AccountRepository;
import com.demoproject.repository.StoreRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.StoreService;
import com.demoproject.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/store")
public class StoreController {
    @Autowired
    private final StoreService storeService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    public StoreController(StoreService storeService, UserService userService, AccountService accountService, AccountRepository accountRepository, StoreRepository storeRepository, UserDetailsService userDetailsService, JwtUtils jwtUtils) {
        this.storeService = storeService;
        this.userService = userService;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.storeRepository = storeRepository;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
    }

    @Autowired
    private JwtUtils jwtUtils;


    @GetMapping("/mystore")
    public String showMyStore(@CookieValue(value = "token", required = false) String token,
                              HttpSession session,
                              Model model) {
        if (token == null) {
            return "redirect:/login";
        }

        String username = jwtUtils.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("OWNER")||role.equals("STAFF")){
            listHiddenPage.add("listOwner");
        }

        if(role.equals("ADMIN")){

            listHiddenPage.add("listCustomer");
            listHiddenPage.add("listProduct");
            listHiddenPage.add("listWarehouse");
            listHiddenPage.add("listBill");
        }
        if(role.equals("ADMIN")||role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage",listHiddenPage);

        if (!jwtUtils.validateToken(token, userDetails)) {
            return "redirect:/login";
        }
        Optional<Account> optAccount= accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        Account account= optAccount.orElse(new Account());
        // Lấy Store của Owner
        Store store = storeService.findStoreByAccount(username);
        System.out.println(store.getName()+" aaaa");
        System.out.println("abc ");
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        model.addAttribute("store", store);
        return "store/myStore";
    }


    @PostMapping("/mystore")
    public String updateStore(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("taxCode") String taxCode,
            Model model) {

        if (token == null) {
            return "redirect:/login";
        }

        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        Account account= optAccount.orElse(new Account());
        if (optAccount.isEmpty()) {
            model.addAttribute("error", "Account not found!");
            return "store/myStore";
        }

        Store existingStore = storeService.findStoreByAccount(username);
        if (existingStore == null) {
            model.addAttribute("error", "Store not found!");
            return "store/myStore";
        }
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("OWNER")||role.equals("STAFF")){
            listHiddenPage.add("listOwner");
        }
        if(role.equals("ADMIN")){
            listHiddenPage.add("listCustomer");
            listHiddenPage.add("listProduct");
            listHiddenPage.add("listWarehouse");
            listHiddenPage.add("listBill");
        }
        if(role.equals("ADMIN")||role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage",listHiddenPage);

        // Cập nhật thông tin
        existingStore.setName(name);
        existingStore.setPhone(phone);
        existingStore.setAddress(address);
        existingStore.setTaxCode(taxCode);

        storeService.save(existingStore);

        model.addAttribute("success", "Store updated successfully!");
        model.addAttribute("store", existingStore);
        model.addAttribute("user", user);
        model.addAttribute("account", account);

        return "store/myStore"; // Giữ nguyên trang sau khi cập nhật
    }




}




