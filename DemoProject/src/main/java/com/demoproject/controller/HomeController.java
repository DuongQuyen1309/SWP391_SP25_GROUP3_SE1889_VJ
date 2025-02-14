package com.demoproject.controller;


import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private final UserService userService;
    private final AccountService accountService;

    public HomeController(AccountService accountService,UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/login")
    public String login() {
        return "login";
    }



    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/resetpw")
    public String resetpw() {
        return "resetpw";
    }

    @GetMapping("/home")
    public String home(@CookieValue(value = "token", required = false) String token,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        String username = jwtUtils.extractUsername(token);
        String role= jwtUtils.extractRole(token);
        System.out.println("role"+role);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("OWNER")||role.equals("STAFF")){
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage",listHiddenPage);
        Optional<Account> account= accountService.findByUsername(username);
        Optional<Users> user= userService.getUserProfile(account.get().getUserId());
        if(user.get().getName()==null|| user.get().getPhone()==null|| user.get().getGender()==null|| user.get().getAddress()==null|| user.get().getDateOfBirth()==null){
            redirectAttributes.addFlashAttribute("alertMessage", "Bạn phải nhập thông tin cá nhân đã");
            return "redirect:/userprofile";
        }
        return "home";
    }


}