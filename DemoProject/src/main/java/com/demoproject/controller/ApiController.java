package com.demoproject.controller;

import com.demoproject.entity.*;
import com.demoproject.entity.Package;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.*;
import com.demoproject.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreService storeService;
    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private CustomerRepository customerRepository;

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

    @GetMapping("/check-taxcode")
    public ResponseEntity<Map<String, Boolean>> checkTaxCodeExists(@RequestParam String taxCode,
                                                                   @CookieValue(value = "token", required = false) String token) {
        Map<String, Boolean> response = new HashMap<>();
        Store store= storeRepository.findByTaxCode(taxCode);
        Long id= Long.parseLong(jwtUtils.extractStoreId(token));

        boolean exists = storeService.existsByTaxCodeExcludingStore(taxCode,id);
        System.out.println(exists);

        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-phone-store")
    public ResponseEntity<Map<String, Boolean>> checkStorePhoneExists(@RequestParam String phone,
                                                                      @CookieValue(value = "token", required = false) String token) {
        Map<String, Boolean> response = new HashMap<>();
        Long id= Long.parseLong(jwtUtils.extractStoreId(token));
        boolean exists = storeService.existsByPhoneExcludingStore(phone,id);


        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-phone-customer")
    public ResponseEntity<Map<String, Boolean>> checkCustomerPhoneExists(@RequestParam String phone,
                                                                      @CookieValue(value = "token", required = false) String token) {
        Map<String, Boolean> response = new HashMap<>();
        Long id= Long.parseLong(jwtUtils.extractStoreId(token));
        List<Customer> customers= customerRepository.findByStoreIdAndIsDeleteFalse(id);
        boolean exists=false;
        if(customers.isEmpty()){
            response.put("exists", exists);
            ResponseEntity.ok(response);
        } else{
            for(Customer customer:customers){
                if(customer.getPhone().equals(phone)){
                    exists=true;
                }
            }
        }

        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-zone")
    public ResponseEntity<Map<String, Boolean>> checkZoneExists(@RequestParam String name,
                                                                         @CookieValue(value = "token", required = false) String token) {
        Map<String, Boolean> response = new HashMap<>();
        Long id= Long.parseLong(jwtUtils.extractStoreId(token));
        List<Zone> zones= zoneRepository.findByStoreId(id);
        boolean exists=false;
        for(Zone z: zones) {
            if(z.getName().equals(name)) {
                exists=true;
            }
        }

        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-package")
    public ResponseEntity<Map<String, Boolean>> checkPackageExists(@RequestParam String name,
                                                                   @CookieValue(value = "token", required = false) String token) {
        Map<String, Boolean> response = new HashMap<>();
        Long id = Long.parseLong(jwtUtils.extractStoreId(token));
        List<Package> packages = packageRepository.findByStoreId(id);
        boolean exists = false;
        for (Package p : packages) {
            if (p.getName().equals(name)) {
                exists = true;
            }
        }

        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-update-package")
    public ResponseEntity<Map<String, Boolean>> checkUpdatePackageExists(@RequestParam String name,
                                                                   @RequestParam String id,
                                                                   @CookieValue(value = "token", required = false) String token) {
        Map<String, Boolean> response = new HashMap<>();
        System.out.println(id);
        Long idPackage = Long.parseLong(id);

        boolean exists = packageRepository.existsByNameAndIdNot(name, idPackage);
        System.out.println("aaaa");
        System.out.println(exists);

        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/packages")
    public List<Package> getAllPackages() {
        return packageRepository.findAll();
    }

}
