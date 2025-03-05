package com.demoproject.controller;

import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.AccountRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.CustomerService;
import com.demoproject.service.EmailService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = accountRepository.existsByUsernameAndIsDeleteFalse(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-old-password")
    public ResponseEntity<Map<String, Boolean>> checkOldPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String oldPassword = request.get("oldPassword");

        boolean valid = false;
        Account account = accountService.findByUsernameAndIsDeleteFalse(username).orElse(null);
        if (account != null && passwordEncoder.matches(oldPassword, account.getPassword())) {
            valid = true;
        }

        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", valid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-phone-account")
    public ResponseEntity<Map<String, Boolean>> checkPhoneExists(@RequestParam String phone, Principal principal) {
        Map<String, Boolean> response = new HashMap<>();

        Account account= accountService.findByUsernameAndIsDeleteFalse(principal.getName()).orElse(null);
        // Lấy user hiện tại từ principal (dựa trên token hoặc session)
        Users currentUser = userService.getUserProfile(account.getUserId()).orElse(null);

        // Kiểm tra nếu có user khác đã dùng số điện thoại này, ngoại trừ user hiện tại
        boolean exists = userService.existsByPhoneExcludingUser(phone, currentUser.getId());

        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-phone-owneraccount")
    public ResponseEntity<Map<String, Boolean>> checkOwnerPhoneExists(@RequestParam String phone,@RequestParam String username) {
        Map<String, Boolean> response = new HashMap<>();

        Account account= accountService.findByUsernameAndIsDeleteFalse(username).orElse(null);

        Users currentUser = userService.getUserProfile(account.getUserId()).orElse(null);

        // Kiểm tra nếu có user khác đã dùng số điện thoại này, ngoại trừ user hiện tại
        boolean exists = userService.existsByPhoneExcludingUser(phone, currentUser.getId());

        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = accountRepository.existsByEmailAndIsDeleteFalse(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer")
    public ResponseEntity<?> checkCustomerByPhone(@RequestParam String phone) {
        Optional<Customer> customer = customerService.getCustomerByPhone(phone);
        if (customer.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "name", customer.get().getName(),
                    "address", customer.get().getAddress()
            ));
        }
        return ResponseEntity.ok(Map.of("exists", false));
    }
}
