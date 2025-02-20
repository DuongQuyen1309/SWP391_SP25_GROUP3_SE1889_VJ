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
        return "login";
    }



    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String displayname,
                           Model model) {

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setDisplayName(displayname);


        // Lưu vào cơ sở dữ liệu
        accountService.createAccount(account);

        return "redirect:/login";
    }

    @GetMapping("/changepw")
    public String changepw() {
        return "changepw";
    }

    @PostMapping("/changepw")
    public String resetPassword(
            @RequestParam("username") String username,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        Optional<Account> optionalAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        boolean isMatch= accountService.checkPassword(username, oldPassword);
        if(!isMatch){
            model.addAttribute("error", "❌ Sai tài khoản hoặc mật khẩu cũ!");
            return "changepw";
        }
        if(oldPassword.equals(newPassword)){
            model.addAttribute("error", "❌ Mật khẩu mới trùng với mật khẩu cũ");
            return "changepw";
        }
        if(!newPassword.equals(confirmPassword)){
            model.addAttribute("error", "❌ Mật khẩu nhập lai không khớp");
            return "changepw";
        }


        Optional<Account> account = optionalAccount;
        account.get().setPassword(passwordEncoder.encode(newPassword)); // Mã hóa mật khẩu mới
        accountRepository.save(account.get());

        model.addAttribute("success", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
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
            listHiddenPage.add("listInvoice");
        }
        if(role.equals("ADMIN")||role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage",listHiddenPage);

        Optional<Account> optAccount= accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user= userService.getUserProfile(optAccount.get().getUserId());
        if(user.get().getName()==null|| user.get().getPhone()==null|| user.get().getGender()==null|| user.get().getAddress()==null|| user.get().getDateOfBirth()==null){
            redirectAttributes.addFlashAttribute("alertMessage", "Bạn phải nhập thông tin cá nhân đã");
            return "redirect:/user/userprofile";
        }
        Account account= optAccount.orElse(null);
        model.addAttribute("account", account);
        return "home";
    }


}