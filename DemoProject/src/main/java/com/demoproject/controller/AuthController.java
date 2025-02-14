package com.demoproject.controller;

import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.repository.AccountRepository;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpServletResponse response) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            Optional<Account> account= accountRepository.findByUsername(username);
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
}
