package com.demoproject.controller;

import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.entity.Zone;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.demoproject.service.ZoneService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class WarehouseController {
    private final AccountService accountService;
    private final UserService userService;
    private final ZoneService zoneService;
    @Autowired
    private final JwtUtils jwtUtils;
    @Autowired
    private AccountRepository accountRepository;

    public WarehouseController(AccountService accountService, UserService userService, ZoneService zoneService, JwtUtils jwtUtils) {
        this.accountService = accountService;
        this.userService = userService;
        this.zoneService=zoneService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/warehouse")
    public String getWarehousePage(@CookieValue(value = "token", required = false) String token,
                                   Model model) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        model.addAttribute("account", optAccount.get());
        model.addAttribute("user", user);
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        return "warehouse/warehouse";

    }
    @PostMapping("/warehouse")
    public String postWarehousePage(@RequestParam String warehouseName, Model model, @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "3") int size, @RequestParam(defaultValue = "") String search,
                                    @CookieValue(value = "token", required = false) String token,
                                    RedirectAttributes redirectAttributes) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Zone> zonePage;
        // Lấy thông tin người dùng từ token
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        if (search.isEmpty()) {
            zonePage = this.zoneService.getAllZonesByWarehouseName(pageable, user.get().getWarehouseName());
        } else {
            zonePage = this.zoneService.getZonesByName(user.get().getWarehouseName(), search, pageable);
        }
        model.addAttribute("zonePage", zonePage);
        model.addAttribute("search", search);
        user.get().setWarehouseName(warehouseName);
        userService.saveUserProfile(user.get());

        return "redirect:/warehouse/listWarehouseZone";

    }
}
