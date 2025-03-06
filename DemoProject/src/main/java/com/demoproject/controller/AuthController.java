package com.demoproject.controller;

import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.repository.AccountRepository;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.service.AccountService;
import com.demoproject.service.EmailService;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
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
    private EmailService emailService;

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



    @PostMapping("/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean isEmailExist= accountRepository.existsByEmailAndIsDeleteFalse(email);

        if (!isEmailExist) {
            return ResponseEntity.status(400).body(Map.of("error", "Email not found!"));
        }

        Account account= accountRepository.findByEmailAndIsDeleteFalse(email).orElse(null);



        if (account.getResetToken() != null && account.getResetTokenExpiry() != null && account.getResetTokenExpiry().isAfter(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message","Reset link has already been sent. Please check your email."));
        }

        String resetToken = jwtUtils.generateToken(account.getUsername(), "RESET");
        account.setResetToken(resetToken);
        account.setResetTokenExpiry(LocalDateTime.now().plusMinutes(5)); // Token có hiệu lực 30 phút
        accountRepository.save(account);

        emailService.sendResetPasswordEmail(email, resetToken);
        return ResponseEntity.ok(Map.of("message", "Reset link has been sent to your email."));
    }

    @PostMapping("/auth/resetpw")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        Optional<Account> accountOpt = accountRepository.findByResetTokenAndIsDeleteFalse(token);
        if (accountOpt.isEmpty() || accountOpt.get().getResetTokenExpiry() == null || accountOpt.get().getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }

        Account account = accountOpt.get();
        account.setPassword(passwordEncoder.encode(newPassword));
        account.setResetToken(null);
        account.setResetTokenExpiry(null);
        accountRepository.save(account);

        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully!"));
    }

}
