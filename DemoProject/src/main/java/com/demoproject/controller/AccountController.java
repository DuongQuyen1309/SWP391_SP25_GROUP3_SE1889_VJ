package com.demoproject.controller;


import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.demoproject.repository.AccountRepository;

import java.time.LocalDate;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


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
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false, name = "search") String search,
            RedirectAttributes redirectAttributes) {

        // ✅ Lấy thông tin người dùng từ token

        Account account = accountService.getAccountFromToken(token).orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());



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

        model.addAttribute("account", account);
        model.addAttribute("user", user.orElse(null));

        return "owner/listOwner";
    }





    @PostMapping("/deleteOwner")
    public String deleteAccount(@RequestParam("id") Long id) {
        Optional<Account> optAccount = accountRepository.findById(id);

        if (optAccount.isPresent()) {
            accountService.deleteAccount(optAccount.orElse(null));
        }

        return "redirect:/account/listOwner"; // Quay lại trang danh sách sau khi xóa
    }


    @GetMapping("/createOwner")
    public String createOwner(@CookieValue(value = "token", required = false) String token,
                              Model model) {

        Account account = accountService.getAccountFromToken(token).orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        model.addAttribute("user",user.orElse(null));
        model.addAttribute("account", account);
        return "owner/createOwner";
    }

    @PostMapping("/createOwner")
    public String createOwner(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String displayname,
                              @RequestParam String email,
                              @CookieValue(value = "token", required = false) String token,
                              Model model) {
        Optional<Account> adAccount= accountService.getAccountFromToken(token);
        model.addAttribute("account", adAccount.get());
        Optional<Users> user = userService.getUserProfile(adAccount.get().getUserId());
        model.addAttribute("user",user.orElse(null));
        Account accountOwner = new Account();
        accountOwner.setUsername(username);
        accountOwner.setPassword(passwordEncoder.encode(password));
        accountOwner.setDisplayName(displayname);
        accountOwner.setEmail(email);
        accountOwner.setCreatedBy(adAccount.get().getId());

        accountService.createAccount(accountOwner);
        return "redirect:/account/listOwner ";


    }

    @GetMapping("/viewOwner")
    public String viewOwner(Model model,
                            @RequestParam String id,
                            @CookieValue(value = "token", required = false) String token){
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Optional<Account> accountOwner= accountService.findById(Long.parseLong(id));
        Optional<Users> userOwnerOpt= userService.getUserProfile(accountOwner.get().getUserId());
        Users user=userOwnerOpt.orElse(null);
        Optional<Users> userOpt= userService.getUserProfile(account.getUserId());
        model.addAttribute("accountOwner", accountOwner.get());
        model.addAttribute("account", account);
        model.addAttribute("userCurrent", userOpt.get());
        model.addAttribute("user", user);
        return "owner/viewOwner";
    }

//    @GetMapping("/updateOwner")
//    public String updateOwner( Model model,
//                               @RequestParam String id,
//                               @CookieValue(value = "token", required = false) String token){
//        // ✅ Lấy thông tin người dùng từ token
//        Account account = accountService.getAccountFromToken(token).orElse(null);
//        Optional<Account> accountOwner= accountService.findById(Long.parseLong(id));
//        Optional<Users> userOpt= userService.getUserProfile(accountOwner.get().getUserId());
//        Users user = userOpt.orElse(new Users());
//        model.addAttribute("accountOwner", accountOwner.get());
//        model.addAttribute("account", account);
//        model.addAttribute("user", user);
//        return "updateOwner";
//    }
//
//    @PostMapping("/updateOwner")
//    public String updateOwner(@RequestParam String username,
//                              @RequestParam String displayname,
//                              @RequestParam String name,
//                              @RequestParam String phone,
//                              @RequestParam String address,
//                              @RequestParam String gender,
//                              @RequestParam String dob,
//                              Model model,
//                              @CookieValue(value = "token", required = false) String token){
//
//        Account account = accountService.getAccountFromToken(token).orElse(null);
//        Optional<Account> optAccountOwner = accountRepository.findByUsernameAndIsDeleteFalse(username);
//        Account accountOwner = optAccountOwner.orElse(null);
//        Optional<Users> userOpt= userService.getUserProfile(accountOwner.getUserId());
//        Users user = userOpt.orElse(new Users());
//        model.addAttribute("account", account);
//        model.addAttribute("accountOwner", accountOwner);
//        model.addAttribute("user", user);
//
//        accountOwner.setDisplayName(displayname);
//        user.setName(name);
//        user.setPhone(phone);
//        user.setAddress(address);
//        user.setDateOfBirth(LocalDate.parse(dob));
//        user.setGender(Boolean.parseBoolean(gender));
//        accountRepository.save(accountOwner);
//        userService.saveUserProfile(user);
//
//return "redirect:/account/updateOwner?id=" + accountOwner.getId();
//    }





    //    List staff
    @GetMapping("/listStaff")
    public String showStaffList(
            Model model,
            @CookieValue(value = "token", required = false) String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false, name = "search") String search,
            RedirectAttributes redirectAttributes) {

        // Lấy thông tin người dùng từ token
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        model.addAttribute("user", user.orElse(null));

        model.addAttribute("account", account);

        // Tìm kiếm hoặc lấy danh sách Staff
        Page<Map<String, Object>> staffPage;
        if (search != null && !search.isEmpty()) {
            staffPage = accountService.searchStaffAccounts(account,search, page, size);
        } else {
            staffPage = accountService.getStaffAccounts(account,page, size);
        }


        // Đưa dữ liệu vào model
        model.addAttribute("accounts", staffPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", staffPage.getTotalPages());
        model.addAttribute("search", search);

        return "staff/listStaff";
    }

    @PostMapping("/deleteStaff")
    public String deleteStaff(@RequestParam("id") Long id) {
        Optional<Account> optAccount = accountRepository.findById(id);

        if (optAccount.isPresent()) {
            accountService.deleteAccount(optAccount.orElse(null));
        }

        return "redirect:/account/listStaff";
    }
    @GetMapping("/createStaff")
    public String createStaff(@CookieValue(value = "token", required = false) String token,
                              Model model) {
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        model.addAttribute("user", user.orElse(null));
        model.addAttribute("account", account);
        return "staff/createStaff"; // Hiển thị trang createStaff.html
    }


    @PostMapping("/createStaff")
    public String createStaff(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String displayname,
                              @RequestParam String email,
                              @CookieValue(value = "token", required = false) String token,
                              Model model) {
        if (token == null || token.isEmpty()) {
            model.addAttribute("error", "Unauthorized: Missing authentication token.");
            return "staff/createStaff"; // Quay lại trang createStaff nếu có lỗi
        }

        String usernameAdmin = jwtUtils.extractUsername(token);
        Optional<Account> adAccount = accountRepository.findByUsernameAndIsDeleteFalse(usernameAdmin);

        if (adAccount.isEmpty()) {
            model.addAttribute("error", "Unauthorized: Admin account not found.");
            return "staff/createStaff"; // Quay lại trang createStaff nếu có lỗi
        }

        // Kiểm tra username có bị trùng không
        Optional<Account> existingAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        if (existingAccount.isPresent()) {
            model.addAttribute("error", "Username already exists.");
            return "staff/createStaff"; // Quay lại trang createStaff nếu có lỗi
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setDisplayName(displayname);
        account.setEmail(email);
        account.setCreatedBy(adAccount.get().getId());



        accountService.createStaffAccount(account);
        return "redirect:/account/listStaff"; // Quay lại danh sách nhân viên sau khi tạo
    }


    @GetMapping("/updateStaff")
    public String updateStaff(Model model, @RequestParam String id,
                              @CookieValue(value = "token", required = false) String token) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        Optional<Users> userCurrent= userService.getUserProfile(account.getUserId());
        Optional<Account> accountStaff = accountService.findById(Long.parseLong(id));
        Optional<Users> userOpt = userService.getUserProfile(accountStaff.get().getUserId());
        Users user = userOpt.orElse(new Users());
        model.addAttribute("account", account);
        model.addAttribute("userCurrent", userCurrent.get());
        model.addAttribute("accountStaff", accountStaff.get());
        model.addAttribute("user", user);
        return "staff/updateStaff";
    }

    @PostMapping("/updateStaff")
    public String updateStaff(@RequestParam String username,
                              @RequestParam String displayname,
                              @RequestParam String name,
                              @RequestParam String phone,
                              @RequestParam String address,
                              @RequestParam String gender,
                              @RequestParam String dob) {
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

        return "redirect:/account/updateStaff?id=" + account.getId();
    }

    @GetMapping("/resetpwStaff")
    public String resetPasswordStaff(Model model,
                                     @RequestParam String id,
                                     @CookieValue(value = "token", required = false) String token) {
        Optional<Account> accountStaff = accountService.findById(Long.parseLong(id));
        String usernameAccount = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(usernameAccount);
        Account account = optAccount.orElse(null);
        Optional<Users> user= userService.getUserProfile(account.getUserId());
        model.addAttribute("user", user.orElse(null));

        model.addAttribute("account", account);
        model.addAttribute("accountStaff", accountStaff.get());

        return "staff/resetpwStaff";
    }

    @PostMapping("/resetpwStaff")
    public String resetPasswordStaff(@RequestParam String username,
                                     @RequestParam String newPassword,
                                     @RequestParam String confirmPassword,
                                     Model model,
                                     @CookieValue(value = "token", required = false) String token) {
        Optional<Account> optAccountStaff = accountRepository.findByUsernameAndIsDeleteFalse(username);
        String usernameAccount = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(usernameAccount);
        Account account = optAccount.orElse(null);

        model.addAttribute("account", account);

        optAccountStaff.get().setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(optAccountStaff.get());

        return "redirect:/account/updateStaff?id=" + optAccountStaff.get().getId();
    }





}
