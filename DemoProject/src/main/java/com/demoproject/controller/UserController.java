package com.demoproject.controller;

import com.demoproject.entity.User;
import com.demoproject.repository.UserRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class UserController {
    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/userprofile")
    public String showProfile( Model model) {
        User user = userService.getUserProfile(Long.parseLong("2"));
        model.addAttribute("user", user);
        return "userprofile";
    }

    @PostMapping("/userprofile")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String phone,
                                @RequestParam String address,
                                @RequestParam String dob,
                                @RequestParam String gender
    ) {
        User user= userService.getUserProfile(Long.parseLong("2"));
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        user.setDateOfBirth(LocalDate.parse(dob));
        user.setGender(Boolean.parseBoolean(gender));
        userService.saveUserProfile(user);


        return "redirect:/userprofile";
    }

}