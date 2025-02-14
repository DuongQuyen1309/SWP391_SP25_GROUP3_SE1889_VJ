package com.demoproject.controller;


import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.demoproject.repository.AccountRepository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;


@Controller
public class AccountController {
    @Autowired
    private final AccountService accountService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private JwtUtils jwtUtils;

    public AccountController(AccountService accountService,UserService userService) {
        this.accountService = accountService;
        this.userService = userService;

    }

    @GetMapping("/listOwner")
    public String showAccountList(
            Model model,
            @CookieValue(value = "token", required = false) String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false, name = "search") String search,
            RedirectAttributes redirectAttributes) {

        // ✅ Lấy thông tin người dùng từ token
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsername(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());

        // ✅ Nếu thiếu thông tin cá nhân, chuyển về `/userprofile`
        if (user.isPresent() && (user.get().getName() == null || user.get().getPhone() == null ||
                user.get().getGender() == null || user.get().getAddress() == null ||
                user.get().getDateOfBirth() == null)) {
            redirectAttributes.addFlashAttribute("alertMessage", "Bạn phải nhập thông tin cá nhân đã");
            return "redirect:/userprofile";
        }

        // ✅ Gọi phương thức tìm kiếm nếu có từ khóa, ngược lại lấy toàn bộ danh sách Owner
        Page<Map<String, Object>> accountPage;
        if (search != null && !search.isEmpty()) {
            accountPage = accountService.searchOwnerAccounts(search, page, size);
        } else {
            accountPage = accountService.getOwnerAccounts(page, size);
        }

        // ✅ Đưa dữ liệu vào model để Thymeleaf hiển thị
        model.addAttribute("accounts", accountPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accountPage.getTotalPages());
        model.addAttribute("search", search);

        return "listOwner";
    }




    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String displayname,
                           Model model) {
        // Kiểm tra xem username đã tồn tại chưa
        if(accountRepository.existsByUsername(username)) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "register"; // Quay lại trang đăng ký với thông báo lỗi
        }
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setDisplayName(displayname);


        // Lưu vào cơ sở dữ liệu
        accountService.createAccount(account);

        return "redirect:/login";
    }
    @PostMapping("/deleteOwner")
    public String deleteAccount(@RequestParam("id") Long id) {
        Optional<Account> optAccount = accountRepository.findById(id);

        if (optAccount.isPresent()) {
            accountService.deleteAccount(optAccount.orElse(null));
        }

        return "redirect:/listOwner"; // Quay lại trang danh sách sau khi xóa
    }



    @PostMapping("/createOwner")
    public String createOwner(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String displayname,
                              @CookieValue(value = "token", required = false) String token,
                              Model model) {
        String usernameAdmin = jwtUtils.extractUsername(token);
        Optional<Account> adAccount= accountRepository.findByUsername(usernameAdmin);
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setDisplayName(displayname);
        account.setCreatedBy(adAccount.get().getId());

        accountService.createAccount(account);
        return "redirect:/listOwner ";


    }

    @GetMapping("/updateOwner")
    public String updateOwner(@RequestParam String id){

        return "updateOwner";
    }

    @PostMapping("/resetpw")
    public String resetPassword(
            @RequestParam("username") String username,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        Optional<Account> optionalAccount = accountRepository.findByUsername(username);

        if (optionalAccount.isEmpty()) {
            model.addAttribute("error", "User không tồn tại!");
            return "resetpw";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu nhập lại không khớp!");
            return "resetpw";
        }

        Optional<Account> account = optionalAccount;
        account.get().setPassword(passwordEncoder.encode(newPassword)); // Mã hóa mật khẩu mới
        accountRepository.save(account.get());

        model.addAttribute("success", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
        return "redirect:/login";
    }



}
