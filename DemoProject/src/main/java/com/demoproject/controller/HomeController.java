package com.demoproject.controller;


import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.AccountRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private final UserService userService;
    private final AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public HomeController(AccountService accountService,UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @Autowired
    private JwtUtils jwtUtils;



    @GetMapping("/login")
    public String login() {
        return "user/login";
    }



    @GetMapping("/register")
    public String register() {
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String displayname,
                           @RequestParam String email,
                           Model model) {

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setDisplayName(displayname);
        account.setEmail(email);



        // Lưu vào cơ sở dữ liệu
        accountService.createAccount(account);

        return "redirect:/login";
    }



    @GetMapping("/home")
    public String home(@CookieValue(value = "token", required = false) String token,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        String username = jwtUtils.extractUsername(token);
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
            listHiddenPage.add("Store");
            listHiddenPage.add("listPackage");
            listHiddenPage.add("listImportedNote");
        }
        if(role.equals("ADMIN")||role.equals("STAFF")){
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Store");
        }
        model.addAttribute("listHiddenPage",listHiddenPage);

        Optional<Account> optAccount= accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user= userService.getUserProfile(optAccount.get().getUserId());
        Account account= optAccount.orElse(null);
        model.addAttribute("account", account);
        model.addAttribute("user",user.get());


        return "user/home";
    }

    @GetMapping("/forgotpw")
    public String forgotpw() {
        return "user/forgotpw";
    }


    @GetMapping("/resetpw")
    public String resetpw() {
        return "user/resetpw";
    }


    @GetMapping("/access-deny")
    public String accessdeny() {
        return "user/access-deny";
    }



        @GetMapping("/error/403")
        public String error403() {
            return "error/403"; // Trỏ đến templates/error/403.html
        }
    @GetMapping("/test-ex")
    public String testException() {
        return "error/500";

    }

}