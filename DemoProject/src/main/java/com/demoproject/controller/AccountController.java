package com.demoproject.controller;


import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            @RequestParam(required = false) String idFrom,
            @RequestParam(required = false) String idTo,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String fullname,
            RedirectAttributes redirectAttributes) {

        // ✅ Lấy thông tin người dùng từ token
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        model.addAttribute("account", account);
        model.addAttribute("user", user.orElse(null));

        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);


        // ✅ Gọi phương thức tìm kiếm nếu có từ khóa, ngược lại lấy toàn bộ danh sách Owner
        Page<Map<String, Object>> accountPage;

        if ((idFrom != null && !idFrom.isEmpty() && !idFrom.matches("\\d+")) ||
                (idTo != null && !idTo.isEmpty() && !idTo.matches("\\d+"))) {

            model.addAttribute("errorMessage", "Id, Phone fields should be positive number. Money field should be negative number");
            accountPage = Page.empty();
            model.addAttribute("accounts", accountPage.getContent());

            model.addAttribute("idFrom", idFrom);
            model.addAttribute("idTo", idTo);
            model.addAttribute("username", username);
            model.addAttribute("displayName", displayName);
            model.addAttribute("email", email);
            model.addAttribute("name", fullname);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", accountPage.getTotalPages());
            model.addAttribute("size", size);
            return "owner/listOwner";
        }
        else {
            if ((idFrom != null && !idFrom.isEmpty()) || (idTo != null && !idTo.isEmpty())
                    || (username != null && !username.isEmpty()) || (username != null)
                    || (displayName != null) || (displayName != null && !displayName.isEmpty())
                    || (email != null && !email.isEmpty()) || (email != null && !email.isEmpty())
                    || (fullname != null && !fullname.isEmpty()) || (fullname != null && !fullname.isEmpty())) {


                Long req_idFrom = (idFrom != null && !idFrom.isBlank() && idFrom.matches("\\d+")) ? Long.valueOf(idFrom) : null;
                Long req_idTo = (idTo != null && !idTo.isBlank() && idTo.matches("\\d+")) ? Long.valueOf(idTo) : null;

                String req_username = (username == null || username.isEmpty())? null: username;
                String req_displayName = (displayName == null || displayName.isEmpty())? null: displayName;
                String req_email = (email == null || email.isEmpty())? null: email;
                String req_fullname = (fullname == null || fullname.isEmpty()) ? null : fullname;

                accountPage = accountService.searchOwnerByAttribute(req_idFrom,req_idTo,req_username,req_displayName,req_email,req_fullname,pageable);
            } else {
                accountPage = accountService.searchOwnerAll( pageable);
            }

            model.addAttribute("accounts", accountPage.getContent());

            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", accountPage.getTotalPages());
            model.addAttribute("size", size);

            model.addAttribute("idFrom", idFrom);
            model.addAttribute("idTo", idTo);
            model.addAttribute("username", username);
            model.addAttribute("displayName", displayName);
            model.addAttribute("email", email);
            model.addAttribute("name", fullname);


            return "owner/listOwner";
        }





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
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
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
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage",listHiddenPage);
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
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
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
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
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
        Account accountOwner = accountService.getAccountFromToken(token).orElse(null);



        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setDisplayName(displayname);
        account.setEmail(email);
        account.setCreatedBy(accountOwner.getId());
        account.setStoreId(accountOwner.getStoreId());



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
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
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
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

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
