package com.demoproject.service;

import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import com.demoproject.entity.Store;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.AccountRepository;
import com.demoproject.repository.StoreRepository;
import com.demoproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StoreRepository storeRepository;

    @Autowired
    private JwtUtils jwtUtils;
    public AccountService(AccountRepository accountRepository,UserRepository userRepository,StoreRepository storeRepository) {

        this.accountRepository = accountRepository;
        this.userRepository= userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.storeRepository = storeRepository;
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


        if(accountRepository.existsByUsernameAndIsDeleteFalse(account.getUsername())){
            return;
        }



        // ✅ Tạo User mới với role = "OWNER" và createdAt = thời điểm hiện tại
        Users newUser= new Users();
        newUser.setCreatedAt(LocalDate.now()); // Đặt thời gian tạo tài khoản
        newUser.setRole("OWNER"); // ✅ Gán role mặc định là "OWNER"



        Store store= new Store();
        store.setCreatedAt(LocalDateTime.now());
        store.setCreatedBy(newUser.getId());
        store = storeRepository.saveAndFlush(store); // **Lưu và lấy storeId**

        store.setStoreId(store.getId()); // Gán Owner ID vào Store ngay khi tạo
        // **3. Gán storeId vào chính Store nếu cần**
        store.setStoreId(store.getId()); // Gán storeId = store.getId()
        storeRepository.save(store); // Cập nhật Store
        System.out.println("User ID: " + newUser.getId());
        System.out.println("Store ID: " + store.getId());


        // **5. Cập nhật User (Owner) với storeId**
        newUser.setStoreId(store.getId());
        userRepository.save(newUser);

        // ✅ Gán userId vào Account
        account.setStoreId(store.getId());
        account.setUserId(newUser.getId());
        account.setCreatedAt(LocalDate.now());
        accountRepository.save(account);
    }

    public void deleteAccount(Account account) {
        account.setDelete(true);
        account.setDeletedAt(LocalDate.now());
        accountRepository.save(account);
    }

    public Optional<Account> findByUsernameAndIsDeleteFalse(String username) {
        return accountRepository.findByUsernameAndIsDeleteFalse(username);
    }

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
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
                    accountData.put("email", account.getEmail());

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
       for (Long userId : ownerUserIds) {
           System.out.println(userId);
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
                    accountData.put("email", account.getEmail());

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

    public boolean checkPassword(String username,String password) {
        Optional<Account> optAccount= accountRepository.findByUsernameAndIsDeleteFalse(username);
        if (!optAccount.isPresent()) {
            return false;
        }
        String hashedPassword= optAccount.get().getPassword();
        return passwordEncoder.matches(password,hashedPassword);
    }

    //them listStaff
    public List<Account> listStaffAccount() {
        List<Long> ids = userRepository.findIdByRole("STAFF");
        List<Account> accounts = accountRepository.findByUserIdIn(ids);
        List<Account> staffs = new ArrayList<>();
        for(Account account : accounts) {
            if(!account.getDelete()) {
                staffs.add(account);
            }
        }
        return staffs;
    }

    public void createStaffAccount(Account account) {
        if (!account.getPassword().startsWith("$2a$")) {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
        }

        if(accountRepository.existsByUsernameAndIsDeleteFalse(account.getUsername())) {
            return;
        }
        // Tạo User mới với role = "STAFF" và createdAt = thời điểm hiện tại
        Users newUser = new Users();
        newUser.setCreatedAt(LocalDate.now());
        newUser.setRole("STAFF"); // Gán role là "STAFF"
        newUser.setStoreId(account.getStoreId());
        userRepository.save(newUser);

        // Gán userId vào Account
        account.setUserId(newUser.getId());
        account.setCreatedAt(LocalDate.now());
        accountRepository.save(account);
    }

    public Page<Map<String, Object>> getStaffAccounts(Account accountOwner, Pageable pageable) {
        // 1. Lấy danh sách userId của các User có role STAFF
        List<Long> staffUserIds = userRepository.getStaffID(accountOwner.getStoreId());
        if (staffUserIds.isEmpty()) {
            return Page.empty(); // Không có staff thì trả về rỗng
        }

        // 2. Lấy danh sách Account của các User có role STAFF
        Page<Account> accountPage = accountRepository.findByUserIdInAndIsDeleteFalse(staffUserIds, pageable);

        // 3. Lấy thông tin Users từ danh sách ID đã tìm được
        Map<Long, Users> userMap = userRepository.findAllById(staffUserIds)
                .stream()
                .collect(Collectors.toMap(Users::getId, user -> user));

        // 4. Chuyển đổi kết quả thành danh sách Map
        List<Map<String, Object>> accountsWithUsers = accountPage.getContent().stream().map(account -> {
            Map<String, Object> accountData = new HashMap<>();
            accountData.put("id", account.getId());
            accountData.put("username", account.getUsername());
            accountData.put("displayName", account.getDisplayName());
            accountData.put("email", account.getEmail());

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
        }).collect(Collectors.toList());

        return new PageImpl<>(accountsWithUsers, pageable, accountPage.getTotalElements());
    }

    /**
     * Tìm kiếm staff theo keyword với phân trang
     */
    public Page<Map<String, Object>> searchStaffAccounts(Account accountOwner, String keyword, Pageable pageable) {
        List<Long> staffUserIds = userRepository.getStaffID(accountOwner.getStoreId());
        if (staffUserIds.isEmpty()) {
            return Page.empty();
        }

        // Tìm kiếm Account có keyword
        Page<Account> accountPage = accountRepository.searchStaffAccounts(keyword, staffUserIds, pageable);

        Map<Long, Users> userMap = userRepository.findAllById(staffUserIds)
                .stream()
                .collect(Collectors.toMap(Users::getId, user -> user));

        List<Map<String, Object>> accountsWithUsers = accountPage.getContent().stream().map(account -> {
            Map<String, Object> accountData = new HashMap<>();
            accountData.put("id", account.getId());
            accountData.put("username", account.getUsername());
            accountData.put("displayName", account.getDisplayName());
            accountData.put("email", account.getEmail());

            Users user = userMap.get(account.getUserId());
            if (user != null) {
                accountData.put("name", user.getName());
                accountData.put("phone", user.getPhone());
                accountData.put("dateOfBirth", user.getDateOfBirth());
                accountData.put("address", user.getAddress());
                accountData.put("gender", user.getGender());
            }

            return accountData;
        }).collect(Collectors.toList());

        return new PageImpl<>(accountsWithUsers, pageable, accountPage.getTotalElements());
    }

    /**
     * Lấy tài khoản từ token
     */
    public Optional<Account> getAccountFromToken(String token) {
        String username = jwtUtils.extractUsername(token);
        return findByUsernameAndIsDeleteFalse(username);
    }

    public void deleteAccountFromToken(String token) {}

    public Page<Map<String, Object>> searchOwnerByAttribute( Long req_idFrom, Long req_idTo, String req_username,
                                                    String req_displayName,  String req_email,
                                                    String req_fullname, Pageable pageable) {
        List<Long> ids=userRepository.findIdByRole("OWNER");
        // 2️⃣ Nếu không có User nào, trả về danh sách rỗng
        if (ids.isEmpty()) {
            System.out.println("ids is empty");
            return Page.empty();
        }
        // ✅ 2. Tìm kiếm danh sách Account của các User có role OWNER
        Page<Account> accountPage =accountRepository.findByAttribute(ids, req_idFrom, req_idTo, req_username,
                req_displayName, req_email, req_fullname, pageable);

        Map<Long, Users> userMap = userRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(Users::getId, user -> user));

        // ✅ 4. Chuyển đổi kết quả thành danh sách Map để gửi về Thymeleaf
        List<Map<String, Object>> accountsWithUsers = accountPage.getContent().stream()
                .map(account -> {
                    Map<String, Object> accountData = new HashMap<>();
                    accountData.put("id", account.getId());
                    accountData.put("username", account.getUsername());
                    accountData.put("displayName", account.getDisplayName());
                    accountData.put("email", account.getEmail());

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

    public Page<Map<String, Object>> searchOwnerAll(Pageable pageable) {
        List<Long> ids=userRepository.findIdByRole("OWNER");
        Page<Account> accountPage = accountRepository.findByUserIdInAndIsDeleteFalse(ids, pageable);
        Map<Long, Users> userMap = userRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(Users::getId, user -> user));

        // ✅ 4. Chuyển đổi kết quả thành danh sách Map để gửi về Thymeleaf
        List<Map<String, Object>> accountsWithUsers = accountPage.getContent().stream()
                .map(account -> {
                    Map<String, Object> accountData = new HashMap<>();
                    accountData.put("id", account.getId());
                    accountData.put("username", account.getUsername());
                    accountData.put("displayName", account.getDisplayName());
                    accountData.put("email", account.getEmail());

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
