package com.demoproject.service;

import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.repository.AccountRepository;
import com.demoproject.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    public AccountService(AccountRepository accountRepository,UserRepository userRepository) {

        this.accountRepository = accountRepository;
        this.userRepository= userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<Account> listOwnerAccount() {
        List<Long> ids=userRepository.findIdByRole("OWNER");
        List<Account> accounts= accountRepository.findByUserIdIn(ids);
        List<Account> owners= new ArrayList<>();
        for(Account account:accounts){
            if(!account.getDelete()){
                owners.add(account);
            }
        }
        return owners;
    }

    public void saveAccount(Account account) {
        if (!account.getPassword().startsWith("$2a$")) { // ✅ Chỉ mã hóa nếu chưa mã hóa
            account.setPassword(passwordEncoder.encode(account.getPassword()));
        }

        // ✅ Tạo User mới với role = "OWNER" và createdAt = thời điểm hiện tại
        Users newUser= new Users();
        newUser.setCreatedAt(LocalDate.now()); // Đặt thời gian tạo tài khoản
        newUser.setRole("OWNER"); // ✅ Gán role mặc định là "OWNER"
        userRepository.save(newUser);

        // ✅ Gán userId vào Account
        account.setUserId(newUser.getId());
        account.setCreatedAt(LocalDate.now());
        accountRepository.save(account);
    }

    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public Page<Account> findByUserIdInAndIsDeleteFalse(Pageable pageable){
        List<Long> ids=userRepository.findIdByRole("OWNER");
        // 2️⃣ Nếu không có User nào, trả về danh sách rỗng
        if (ids.isEmpty()) {
            return Page.empty();
        }

        // 3️⃣ Lọc tài khoản dựa trên danh sách ID của Users và isDelete = false
        return accountRepository.findByUserIdInAndIsDeleteFalse(ids, pageable);
    }

}
