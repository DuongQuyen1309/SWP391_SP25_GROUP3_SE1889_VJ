package com.demoproject.controller;


import com.demoproject.entity.Account;
import com.demoproject.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.demoproject.repository.AccountRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Controller
public class AccountController {
    @Autowired
    private final AccountService accountService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private AccountRepository accountRepository;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/listOwner")
    public String showAccountList(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Account> accountPage = accountService.findByUserIdInAndIsDeleteFalse(pageable);

        model.addAttribute("accounts", accountPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accountPage.getTotalPages());

        return "listOwner";
    }




    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String displayname) {
        // Kiểm tra xem username đã tồn tại chưa
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setDisplayName(displayname);


        // Lưu vào cơ sở dữ liệu
        accountService.saveAccount(account);

        return "redirect:/login";
    }
    @PostMapping("/deleteOwner")
    public String deleteAccount(@RequestParam("id") Long id) {
        Account optAccount = accountRepository.findById(id);

        if (optAccount!=null) {
            Account account = optAccount;
            account.setDelete(true); // Chỉ đánh dấu là đã xóa
            accountRepository.save(account); // Cập nhật lại database
        }

        return "redirect:/listOwner"; // Quay lại trang danh sách sau khi xóa
    }



    @PostMapping("/createOwner")
    public String createOwner(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String displayname) {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setDisplayName(displayname);
        accountService.saveAccount(account);
        return "redirect:/listOwner ";


    }

    @PostMapping("/resetpw")
    public String resetPassword(
            @RequestParam("username") String username,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        Account optionalAccount = accountRepository.findByUsername(username);

        if (optionalAccount==null) {
            model.addAttribute("error", "User không tồn tại!");
            return "resetpw";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu nhập lại không khớp!");
            return "resetpw";
        }

        Account account = optionalAccount;
        account.setPassword(passwordEncoder.encode(newPassword)); // Mã hóa mật khẩu mới
        accountRepository.save(account);

        model.addAttribute("success", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
        return "redirect:/login";
    }



}
