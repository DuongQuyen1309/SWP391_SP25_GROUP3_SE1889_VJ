package com.demoproject.service;

import com.demoproject.dto.StoreDTO;
import com.demoproject.entity.Account;
import com.demoproject.entity.Store;
import com.demoproject.repository.AccountRepository;
import com.demoproject.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class StoreService {
    private final StoreRepository storeRepository;
    private final AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;

    public StoreService(StoreRepository storeRepository, AccountRepository accountRepository, AccountService accountService) {
        this.storeRepository = storeRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

//    public Page<StoreDTO> findStores(String name, String phone, Pageable pageable) {
//        Page<Store> storePage;
//        if ((name == null || name.isBlank()) && (phone == null || phone.isBlank())) {
//            storePage = storeRepository.findAll(pageable);
//        } else {
//            storePage = storeRepository.findByNameOrPhone(name, phone, pageable);
//        }
//
//        // Chuyển đổi từ Store sang StoreDTO
//        return storePage.map(store -> {
//            StoreDTO dto = new StoreDTO();
//            dto.setId(store.getId());
//            dto.setName(store.getName());
//            dto.setAddress(store.getAddress());
//            dto.setPhone(store.getPhone());
//            dto.setOwnerId(store.getStoreId()); // Lưu Owner ID
//            dto.setOwnerName(getOwnerNameByStoreId(store.getStoreId())); // Lấy Owner Name
//            dto.setTaxCode(store.getTaxCode());
//            return dto;
//        });
//    }

    public void deleteStore(Long id) {
        if (storeRepository.existsById(id)) {
            storeRepository.deleteById(id);
        } else {
            throw new RuntimeException("Store not found!");
        }
    }

    public void createStore(StoreDTO storeDTO) {
        Store store = new Store();
        store.setName(storeDTO.getName());
        store.setAddress(storeDTO.getAddress());
        store.setPhone(storeDTO.getPhone());
        store.setTaxCode(storeDTO.getTaxCode()); // Lưu tax_code

        store = storeRepository.save(store); // Lưu Store
        // Lấy tên chủ cửa hàng từ storeId
        String ownerName = storeRepository.findOwnerNameByStoreId(store.getId());
        // Cập nhật Store ID cho Owner
        Account owner = accountRepository.findById(storeDTO.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        owner.setStoreId(store.getId());
        accountRepository.save(owner);
    }

    public Optional<Store> findById(Long id) {
        return storeRepository.findById(id);
    }

    public String getOwnerNameByStoreId(Long storeId) {
        return accountRepository.getOwnerNameByStoreId(storeId)
                .map(account -> {
                    System.out.println("Owner ID: " + storeId + " | Owner Name: " + account.getDisplayName());
                    return account.getDisplayName();
                })
                .orElse("Unknown Owner" );

    }

//    public void updateStore(StoreDTO storeDTO) {
//        Optional<Store> storeOpt = storeRepository.findById(storeDTO.getId());
//        if (storeOpt.isPresent()) {
//            Store store = storeOpt.get();
//
//            // Kiểm tra độ dài của từng trường trước khi cập nhật
//            if (storeDTO.getName() == null || storeDTO.getName().trim().isEmpty()) {
//                throw new IllegalArgumentException("Store name cannot be empty.");
//            }
//            if (storeDTO.getName().length() > 255 || storeDTO.getName() == null) {
//                throw new IllegalArgumentException("Store name must be less than 255 characters.");
//            }
//            // Kiểm tra số điện thoại phải đúng 10 số
//            if (storeDTO.getPhone() == null || !storeDTO.getPhone().matches("\\d{10}")) {
//                throw new IllegalArgumentException("Phone number must be exactly 10 digits!");
//            }
//            // Kiểm tra địa chỉ không được để trống và không vượt quá 500 ký tự
//            if (storeDTO.getAddress() == null || storeDTO.getAddress().trim().isEmpty()) {
//                throw new IllegalArgumentException("Address cannot be empty.");
//            }
//            if (storeDTO.getAddress().length() > 500 || storeDTO.getAddress() == null) {
//                throw new IllegalArgumentException("Address must be less than 500 characters.");
//            }
//
//            store.setName(storeDTO.getName());
//            store.setPhone(storeDTO.getPhone());
//            store.setAddress(storeDTO.getAddress());
//
//            // Kiểm tra taxCode có đúng 10 hoặc 13 ký tự không
//            if (storeDTO.getTaxCode() != null && !storeDTO.getTaxCode().matches("\\d{10}|\\d{13}")) {
//                throw new IllegalArgumentException("Tax code must be exactly 10 or 13 digits.");
//            }
//            store.setTaxCode(storeDTO.getTaxCode()); // Cập nhật tax_code
//
//            storeRepository.save(store);
//        } else {
//            throw new IllegalArgumentException("Store not found with ID: " + storeDTO.getId());
//        }
//    }

//    public Store findByOwnerId(Long storeId) {
//        return storeRepository.findByStoreId(storeId);
//    }

    public Store findStoreByAccount(String username) {
        Optional<Account> accountOpt = accountService.findByUsernameAndIsDeleteFalse(username);
        if (accountOpt.isPresent()) {
            System.out.println("aaaa");
            Long storeId = accountOpt.get().getStoreId(); // Lấy storeId từ Account
            System.out.println(storeId);
            Optional<Store> storeOpt= storeRepository.findById(storeId); // Tìm Store theo storeId
            Store store= storeOpt.get();
            return store;

        }
        return null;
    }


    public void updateStoree(Store store) {
        storeRepository.save(store);
    }
    public void save(Store store) {
        storeRepository.save(store);
    }
}
