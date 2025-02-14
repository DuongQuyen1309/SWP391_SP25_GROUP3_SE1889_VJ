package com.demoproject.service;

import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.repository.AccountRepository;
import com.demoproject.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDate;

import java.util.*;
import java.util.stream.Collectors;

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

    public void createAccount(Account account) {
        if (!account.getPassword().startsWith("$2a$")) { // ✅ Chỉ mã hóa nếu chưa mã hóa
            account.setPassword(passwordEncoder.encode(account.getPassword()));
        }
        Optional<Account> existingAccount=accountRepository.findByUsername(account.getUsername());
        if(existingAccount.isPresent()){
            if(account.getUsername().equals(existingAccount.get().getUsername()) ){
                return;
            }
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

    public void deleteAccount(Account account) {
        account.setDelete(true);
        account.setDeletedAt(LocalDate.now());
        accountRepository.save(account);
    }

    public Optional<Account> findByUsername(String username) {
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

    public Page<Map<String, Object>> getOwnerAccounts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Lấy danh sách userId của các User có role OWNER
        List<Long> ownerUserIds = userRepository.findIdByRole("OWNER");

        // Tìm danh sách Account của các User có role OWNER
        Page<Account> accountPage = accountRepository.findByUserIdInAndIsDeleteFalse(ownerUserIds, pageable);


        Map<Long, Users> userMap = userRepository.findAllById(ownerUserIds)
                .stream()
                .collect(Collectors.toMap(Users::getId, user -> user));

        // Chuyển dữ liệu thành Map chứa Account + User Info
        List<Map<String, Object>> accountsWithUsers = accountPage.getContent().stream()
                .map(account -> {
                    Map<String, Object> accountData = new HashMap<>();
                    accountData.put("id", account.getId());
                    accountData.put("username", account.getUsername());
                    accountData.put("password", account.getPassword());
                    accountData.put("displayName", account.getDisplayName());

                    // Lấy thông tin User từ userMap
                    Users user = userMap.get(account.getUserId());
                    if (user != null) {
                        accountData.put("name", user.getName());
                        accountData.put("phone", user.getPhone());
                        accountData.put("dateOfBirth", user.getDateOfBirth());
                        accountData.put("address", user.getAddress());
                        accountData.put("gender", user.getGender());
                    } else {
                        accountData.put("name", "Unknown");
                        accountData.put("dateOfBirth", null);
                        accountData.put("address", "Unknown");
                        accountData.put("gender", "Unknown");
                        accountData.put("phone", "Unknown");
                    }
                    return accountData;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(accountsWithUsers, pageable, accountPage.getTotalElements());
    }

    public Page<Map<String, Object>> searchOwnerAccounts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // ✅ 1. Lấy danh sách userId của các User có role OWNER
        List<Long> ownerUserIds = userRepository.findIdByRole("OWNER");
        if (ownerUserIds.isEmpty()) {
            return Page.empty(); // Không có Owner nào, trả về danh sách rỗng
        }

        // ✅ 2. Tìm kiếm danh sách Account của các User có role OWNER
        Page<Account> accountPage = accountRepository.searchOwnerAccounts(keyword, ownerUserIds, pageable);

        // ✅ 3. Lấy thông tin Users từ danh sách ID đã tìm được
        Map<Long, Users> userMap = userRepository.findAllById(ownerUserIds)
                .stream()
                .collect(Collectors.toMap(Users::getId, user -> user));

        // ✅ 4. Chuyển đổi kết quả thành danh sách Map để gửi về Thymeleaf
        List<Map<String, Object>> accountsWithUsers = accountPage.getContent().stream()
                .map(account -> {
                    Map<String, Object> accountData = new HashMap<>();
                    accountData.put("id", account.getId());
                    accountData.put("username", account.getUsername());
                    accountData.put("displayName", account.getDisplayName());

                    // Lấy thông tin User từ userMap
                    Users user = userMap.get(account.getUserId());
                    if (user != null) {
                        accountData.put("name", user.getName());
                        accountData.put("phone", user.getPhone());
                        accountData.put("dateOfBirth", user.getDateOfBirth());
                        accountData.put("address", user.getAddress());
                        accountData.put("gender", user.getGender());
                    } else {
                        accountData.put("name", "Unknown");
                        accountData.put("phone", "N/A");
                        accountData.put("dateOfBirth", null);
                        accountData.put("address", "N/A");
                        accountData.put("gender", "N/A");
                    }
                    return accountData;
                })
                .collect(Collectors.toList());

        // ✅ 5. Trả về kết quả tìm kiếm dưới dạng `Page<Map<String, Object>>`
        return new PageImpl<>(accountsWithUsers, pageable, accountPage.getTotalElements());
    }


}
