package com.demoproject.controller;

import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.AccountRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.Map;


import java.time.LocalDateTime;
@Controller
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

        if (!jwtUtils.validateToken(token, userDetails)) {
            return "redirect:/login";
        }
        Account account= accountService.findByUsername(username);
        Users user= userService.getUserProfile(account.getUserId());

        model.addAttribute("user", user);
        return "userprofile";
    }

    @PostMapping("/userprofile")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String phone,
                                @RequestParam String address,
                                @RequestParam String dob,
                                @RequestParam String gender,@CookieValue(value = "token", required = false) String token
    ) {
        String username = jwtUtils.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Account account= accountService.findByUsername(username);
        Users user= userService.getUserProfile(account.getUserId());
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        user.setDateOfBirth(LocalDate.parse(dob));
        user.setGender(Boolean.parseBoolean(gender));
        userService.saveUserProfile(user);


        return "redirect:/userprofile";
    }




}