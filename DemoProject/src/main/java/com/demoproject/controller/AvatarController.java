package com.demoproject.controller;

import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.AccountRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
public class AvatarController {
    @Autowired
    private final AccountService accountService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private JwtUtils jwtUtils;

    public AvatarController(UserService userService, AccountService accountService, AccountRepository accountRepository) {
        this.userService = userService;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    @PostMapping("/user/upload-avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("avatar") MultipartFile file,
                                                            @CookieValue(value = "token", required = false) String token) {
        Map<String, String> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("success", "false");
            response.put("message", "File is empty");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Lấy tên đăng nhập từ token
            String username = jwtUtils.extractUsername(token);
            Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);

            if (optAccount.isEmpty()) {
                response.put("success", "false");
                response.put("message", "User not found");
                return ResponseEntity.status(401).body(response);
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.contains(".") ?
                    originalFileName.substring(originalFileName.lastIndexOf(".")) : ".png"; // Mặc định PNG nếu không có phần mở rộng

            // Tạo tên file ngắn gọn
            String fileName = UUID.randomUUID().toString().substring(0, 8) + fileExtension;
            String uploadDir = "uploads/images/";


            // Tạo thư mục nếu chưa có
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Lưu file vào thư mục
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Cập nhật đường dẫn avatar trong database
            Users user = userService.getUserProfile(optAccount.get().getUserId()).get();
            String imageUrl = "/images/" + fileName;
            user.setAvatar(imageUrl);
            userService.saveUserProfile(user);

            response.put("success", "true");
            response.put("imageUrl", imageUrl); // Trả về đường dẫn ảnh mới
            response.put("message", "Avatar updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", "false");
            response.put("message", "Error uploading file");
            return ResponseEntity.status(500).body(response);
        }
    }
    }


