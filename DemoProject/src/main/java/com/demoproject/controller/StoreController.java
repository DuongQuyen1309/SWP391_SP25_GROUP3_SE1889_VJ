package com.demoproject.controller;

import com.demoproject.dto.StoreDTO;
import com.demoproject.entity.Account;
import com.demoproject.entity.Store;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.AccountRepository;
import com.demoproject.repository.StoreRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.StoreService;
import com.demoproject.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/store")
public class StoreController {
    @Autowired
    private final StoreService storeService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private StoreRepository storeRepository;

@Autowired
private UserDetailsService userDetailsService;

    public StoreController(StoreService storeService, UserService userService, AccountService accountService, AccountRepository accountRepository, StoreRepository storeRepository, UserDetailsService userDetailsService, JwtUtils jwtUtils) {
        this.storeService = storeService;
        this.userService = userService;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.storeRepository = storeRepository;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
    }

    @Autowired
    private JwtUtils jwtUtils;

//    @GetMapping("/list")
//    public String list(Model model, @RequestParam(defaultValue = "0") int page,
//                       @RequestParam(defaultValue = "5") int size,
//                       @RequestParam(required = false) String name,
//                       @RequestParam(required = false) String phone,
//                       @RequestParam(required = false) String address,
//                       @RequestParam(required = false) String owner,
//                       @RequestParam(required = false) String taxCode,
//                       @CookieValue(value = "token", required = false) String token) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
//        Page<StoreDTO> storePage = storeService.findStores(name, phone, pageable);
//
//        model.addAttribute("stores", storePage.getContent()); // Gửi danh sách StoreDTO có ownerName
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", storePage.getTotalPages());
//        model.addAttribute("size", size);
//        model.addAttribute("name", name);
//        model.addAttribute("phone", phone);
//
//        Account account = accountService.getAccountFromToken(token).orElse(null);
//        Optional<Users> user = userService.getUserProfile(account.getUserId());
//        model.addAttribute("account", account);
//        model.addAttribute("user", user.orElse(null));
//
//        return "store/listStore";
//    }
//
//
//    @PostMapping("/delete")
//    public String deleteStore(@RequestParam("id") Long id) {
//        storeService.deleteStore(id);
//        return "redirect:/store/list";
//    }
//
//
//    @GetMapping("/create")
//    public String showCreateForm(Model model) {
//        model.addAttribute("store", new StoreDTO());
//        model.addAttribute("owners", accountService.listOwnerAccount()); // Lấy danh sách chủ cửa hàng
//        return "store/createStore";
//    }
//
//    @PostMapping("/create")
//    public String createStore(@ModelAttribute StoreDTO storeDTO, RedirectAttributes redirectAttributes) {
//        storeService.createStore(storeDTO);
//        redirectAttributes.addFlashAttribute("successMessage", "Store created successfully!");
//        return "redirect:/store/list";
//    }
//
//    @GetMapping("/view")
//    public String viewStore(@RequestParam("id") Long id, Model model) {
//        Optional<Store> storeOpt = storeService.findById(id);
//        if (storeOpt.isPresent()) {
//            model.addAttribute("store", storeOpt.get());
//            return "store/view";
//        }
//        return "redirect:/store/list";
//    }
//
//    @GetMapping("/edit/{id}")
//    public String showEditForm(@PathVariable("id") Long id, Model model,@CookieValue(value = "token", required = false) String token) {
//        Optional<Store> storeOpt = storeService.findById(id);
//        if (storeOpt.isPresent()) {
//            Store store = storeOpt.get();
//            StoreDTO storeDTO = new StoreDTO();
//            storeDTO.setId(store.getId());
//            storeDTO.setName(store.getName());
//            storeDTO.setPhone(store.getPhone());
//            storeDTO.setAddress(store.getAddress());
//            storeDTO.setTaxCode(store.getTaxCode());
//            storeDTO.setOwnerId(store.getStoreId());
//            Account account = accountService.getAccountFromToken(token).orElse(null);
//            Optional<Users> user = userService.getUserProfile(account.getUserId());
//            model.addAttribute("account", account);
//            model.addAttribute("user", user.orElse(null));
//            model.addAttribute("store", storeDTO);
//            model.addAttribute("owners", accountService.listOwnerAccount());
//
//            return "store/editStore";
//        }
//        return "redirect:/store/list";
//    }



//    @PostMapping("/edit")
//    public String updateStore(@ModelAttribute StoreDTO storeDTO,
//                              RedirectAttributes redirectAttributes,
//                              Model model,
//                              @CookieValue(value = "token", required = false) String token) {
//        try {
//            storeService.updateStore(storeDTO);
//            redirectAttributes.addFlashAttribute("successMessage", "Store updated successfully!");
//            return "redirect:/store/list";
//        } catch (IllegalArgumentException e) {
//            // Nếu có lỗi, quay lại trang sửa, giữ lại dữ liệu và gửi thông tin tài khoản
//            Account account = (token != null) ? accountService.getAccountFromToken(token).orElse(null) : null;
//            Users user = (account != null) ? userService.getUserProfile(account.getUserId()).orElse(null) : null;
//
//            model.addAttribute("account", account);
//            model.addAttribute("user", user);
//            model.addAttribute("store", storeDTO);
//            model.addAttribute("owners", accountService.listOwnerAccount());
//            model.addAttribute("errorMessage", e.getMessage()); // Gửi lỗi ra view
//
//            return "store/editStore"; // Quay lại trang chỉnh sửa
//        }
//    }

    @GetMapping("/mystore")
    public String showMyStore(@CookieValue(value = "token", required = false) String token,
                                 HttpSession session,
                                 Model model) {
        if (token == null) {
            return "redirect:/login";
        }

        String username = jwtUtils.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("OWNER")||role.equals("STAFF")){
            listHiddenPage.add("listOwner");
        }
        if(role.equals("ADMIN")){

            listHiddenPage.add("listCustomer");
            listHiddenPage.add("listProduct");
            listHiddenPage.add("listWarehouse");
            listHiddenPage.add("listBill");
        }
        if(role.equals("ADMIN")||role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage",listHiddenPage);

        if (!jwtUtils.validateToken(token, userDetails)) {
            return "redirect:/login";
        }
        Optional<Account> optAccount= accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        Account account= optAccount.orElse(new Account());
        // Lấy Store của Owner
        Store store = storeService.findStoreByAccount(username);
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        model.addAttribute("store", store);
        return "store/myStore";
    }


    @PostMapping("/mystore/update")
    public String updateStore(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("taxCode") String taxCode,
            Model model) {

        if (token == null) {
            return "redirect:/login";
        }

        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        Account account= optAccount.orElse(new Account());
        if (optAccount.isEmpty()) {
            model.addAttribute("error", "Account not found!");
            return "store/myStore";
        }

        Store existingStore = storeService.findStoreByAccount(username);
        if (existingStore == null) {
            model.addAttribute("error", "Store not found!");
            return "store/myStore";
        }

        // Cập nhật thông tin
        existingStore.setName(name);
        existingStore.setPhone(phone);
        existingStore.setAddress(address);
        existingStore.setTaxCode(taxCode);

        storeService.save(existingStore);

        model.addAttribute("success", "Store updated successfully!");
        model.addAttribute("store", existingStore);
        model.addAttribute("user", user);
        model.addAttribute("account", account);

        return "store/myStore"; // Giữ nguyên trang sau khi cập nhật
    }




}




