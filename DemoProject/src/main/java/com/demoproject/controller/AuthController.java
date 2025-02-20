package com.demoproject.controller;

import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.repository.AccountRepository;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpServletResponse response) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            Optional<Account> account= accountRepository.findByUsernameAndIsDeleteFalse(username);
            Optional<Users> user= userService.getUserProfile(account.get().getUserId());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtils.generateToken(username, user.get().getRole());

            // ✅ Lưu token vào Cookie
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // ✅ Đổi thành true nếu dùng HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 ngày
            response.addCookie(cookie);

            return ResponseEntity.ok(Map.of("message", "Đăng nhập thành công!"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Sai tài khoản hoặc mật khẩu!"));
        }
    }

    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie jwtCookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)  // Xóa token
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        return ResponseEntity.ok("Logged out successfully!");
    }

    @PostMapping("/clear-alert-message")
    @ResponseBody
    public ResponseEntity<Void> clearAlertMessage(HttpSession session) {
        session.removeAttribute("alertMessage");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = accountRepository.existsByUsernameAndIsDeleteFalse(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/check-old-password")
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

    @GetMapping("/api/check-phone-account")
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

    @GetMapping("/api/check-phone-owneraccount")
    public ResponseEntity<Map<String, Boolean>> checkOwnerPhoneExists(@RequestParam String phone,@RequestParam String username) {
        Map<String, Boolean> response = new HashMap<>();

        Account account= accountService.findByUsernameAndIsDeleteFalse(username).orElse(null);

        Users currentUser = userService.getUserProfile(account.getUserId()).orElse(null);

        // Kiểm tra nếu có user khác đã dùng số điện thoại này, ngoại trừ user hiện tại
        boolean exists = userService.existsByPhoneExcludingUser(phone, currentUser.getId());

        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

}
