package com.demoproject.service;


import com.demoproject.entity.Account;
import com.demoproject.repository.AccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Account> accountOpt = accountRepository.findByUsernameAndIsDeleteFalse(username);

        if (accountOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        Optional<Account> account = accountOpt;

        // ✅ Tạo `UserDetails` để Spring Security sử dụng
        return User.builder()
                .username(account.get().getUsername())
                .password(account.get().getPassword()) // Mật khẩu đã mã hóa
                .roles("USER") // Gán quyền mặc định "USER"
                .build();
    }

    // ✅ Hàm mã hóa mật khẩu trước khi lưu
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // ✅ Hàm kiểm tra mật khẩu nhập vào với mật khẩu trong DB
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}