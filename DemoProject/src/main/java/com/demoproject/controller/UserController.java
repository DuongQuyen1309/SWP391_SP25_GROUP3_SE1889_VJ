package com.demoproject.controller;

import com.demoproject.dto.UsersDTO;
import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.AccountRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.time.LocalDate;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private final UserService userService;
    private final AccountService accountService;
    @Autowired
    private UserDetailsService userDetailsService; // Thêm Autowired để lấy thông tin user
    @Autowired
    private AccountRepository accountRepository;

    public UserController(UserService userService,AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @Autowired
    private JwtUtils jwtUtils;


    @GetMapping("/userprofile")
    public String getUserProfile(@CookieValue(value = "token", required = false) String token, Model model) {
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
            listHiddenPage.add("listproduct");
            listHiddenPage.add("listWarehouse");
            listHiddenPage.add("listInvoice");
        }
        if(role.equals("ADMIN")||role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage",listHiddenPage);

        if (!jwtUtils.validateToken(token, userDetails)) {
            return "redirect:/login";
        }
        Optional<Account> account= accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(account.get().getUserId());
        Users user = userOpt.orElse(new Users());
        model.addAttribute("user", user);
        return "userprofile";
    }

    @PostMapping("/userprofile")
    public String updateProfile(@Valid @ModelAttribute UsersDTO userDTO,
                                BindingResult result
            ,@RequestParam String name,
                                @RequestParam String phone,
                                @RequestParam String address,
                                @RequestParam String dob,
                                @RequestParam String gender,@CookieValue(value = "token", required = false) String token
    ) {

        String username = jwtUtils.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Optional<Account> account= accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user= userService.getUserProfile(account.get().getUserId());
        user.get().setName(name);
        user.get().setPhone(phone);
        user.get().setAddress(address);
        user.get().setDateOfBirth(LocalDate.parse(dob));
        user.get().setGender(Boolean.parseBoolean(gender));
        userService.saveUserProfile(user.orElse(null));


        return "redirect:/user/userprofile";
    }




}