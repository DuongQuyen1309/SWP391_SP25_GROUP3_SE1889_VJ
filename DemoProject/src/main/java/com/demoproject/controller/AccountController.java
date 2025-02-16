package com.demoproject.controller;


import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.demoproject.repository.AccountRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;


@Controller
@RequestMapping("/account")
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
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());

        // ✅ Nếu thiếu thông tin cá nhân, chuyển về `/userprofile`
        if (user.isPresent() && (user.get().getName() == null || user.get().getPhone() == null ||
                user.get().getGender() == null || user.get().getAddress() == null ||
                user.get().getDateOfBirth() == null)) {
            redirectAttributes.addFlashAttribute("alertMessage", "Bạn phải nhập thông tin cá nhân đã");
            return "redirect:/user/userprofile";
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





    @PostMapping("/deleteOwner")
    public String deleteAccount(@RequestParam("id") Long id) {
        Optional<Account> optAccount = accountRepository.findById(id);

        if (optAccount.isPresent()) {
            accountService.deleteAccount(optAccount.orElse(null));
        }

        return "redirect:/account/listOwner"; // Quay lại trang danh sách sau khi xóa
    }



    @PostMapping("/createOwner")
    public String createOwner(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String displayname,
                              @CookieValue(value = "token", required = false) String token,
                              Model model) {
        String usernameAdmin = jwtUtils.extractUsername(token);
        Optional<Account> adAccount= accountRepository.findByUsernameAndIsDeleteFalse(usernameAdmin);
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setDisplayName(displayname);
        account.setCreatedBy(adAccount.get().getId());

        accountService.createAccount(account);
        return "redirect:/account/listOwner ";


    }

    @GetMapping("/updateOwner")
    public String updateOwner( Model model,@RequestParam String id){

        Optional<Account> account= accountService.findById(Long.parseLong(id));
        Optional<Users> userOpt= userService.getUserProfile(account.get().getUserId());
        Users user = userOpt.orElse(new Users());
        model.addAttribute("account", account.get());
        model.addAttribute("user", user);
        return "updateOwner";
    }

    @PostMapping("/updateOwner")
    public String updateOwner(@RequestParam String username,
                              @RequestParam String displayname,
                              @RequestParam String name,
                              @RequestParam String phone,
                              @RequestParam String address,
                              @RequestParam String gender,
                              @RequestParam String dob){
        Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        account.setDisplayName(displayname);
        user.get().setName(name);
        user.get().setPhone(phone);
        user.get().setAddress(address);
        user.get().setDateOfBirth(LocalDate.parse(dob));
        user.get().setGender(Boolean.parseBoolean(gender));
        accountRepository.save(account);
        userService.saveUserProfile(user.orElse(null));

return "redirect:/account/updateOwner?id=" + account.getId();
    }



    @GetMapping("/resetpwOwner")
    public String resetPasswordOwner(Model model,@RequestParam String id ){
        Optional<Account> account= accountService.findById(Long.parseLong(id));
        model.addAttribute("account", account.get());
        return "resetpwOwner";
    }

    @PostMapping("/resetpwOwner")
    public String resetPasswordOwner(@RequestParam String username,
                                     @RequestParam String newPassword,
                                     @RequestParam String confirmPassword,
                                     Model model) {
        Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);

        optAccount.get().setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(optAccount.get());

        return "redirect:/account/updateOwner?id=" + optAccount.get().getId();
    }
}
