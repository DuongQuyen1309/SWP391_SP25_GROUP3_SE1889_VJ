package com.demoproject.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.demoproject.entity.Account;
import com.demoproject.entity.Product;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.service.AccountService;
import com.demoproject.service.ProductService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import org.springframework.data.domain.Pageable;

import com.demoproject.entity.Zone;
import com.demoproject.service.ZoneService;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/warehouse")
public class ZoneController {
    private ZoneService zoneService;
    private final AccountService accountService;
    private final UserService userService;
    private final ProductService productService;
    @Autowired
    private JwtUtils jwtUtils;

    public ZoneController(ZoneService zoneService, AccountService accountService, UserService userService,
                          ProductService productService) {
        this.zoneService = zoneService;
        this.accountService = accountService;
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping("/listWarehouseZone")
    public String getAllZone(Model model, @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "3") int size, @RequestParam(defaultValue = "") String search,
                             @RequestParam(defaultValue = "name") String searchBy,
                             @CookieValue(value = "token", required = false) String token,
                             RedirectAttributes redirectAttributes) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Zone> zonePage;
        // Lấy thông tin người dùng từ token
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        if (search.isEmpty()) {
            zonePage = this.zoneService.getAllZonesByStoreID(pageable, user.getStoreId());
        } else {
            if (searchBy.equals("name")) {
                zonePage = this.zoneService.getZonesByName(search, user.getStoreId(), pageable);
            } else {
                zonePage = this.zoneService.getZonesByPosition(search, user.getStoreId(), pageable);
            }
        }
        model.addAttribute("zonePage", zonePage);
        model.addAttribute("search", search);
        model.addAttribute("searchBy", searchBy);
        return "warehouse/zone";
    }

    @GetMapping("/createWarehouseZone")
    public String getCreateZonePage(Model model, @CookieValue(value = "token", required = false) String token) {
        model.addAttribute("newZone", new Zone());
        String role = jwtUtils.extractRole(token);
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        model.addAttribute("user", user.get());
        model.addAttribute("account", account.get());
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        return "warehouse/create";
    }

    @PostMapping("/createWarehouseZone")
    public String postMethodName(Model model, @ModelAttribute("newZone") Zone newZone,
                                 @CookieValue(value = "token", required = false) String token,
                                 RedirectAttributes redirectAttributes) {
        try {
            String role = jwtUtils.extractRole(token);
            List<String> listHiddenPage = new ArrayList<>();
            listHiddenPage.add("");
            if (role.equals("STAFF")) {
                listHiddenPage.add("listStaff");
            }
            model.addAttribute("listHiddenPage", listHiddenPage);
            String username = jwtUtils.extractUsername(token);
            Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
            Users user = userService.getUserProfile(account.get().getUserId()).orElse(null);
            newZone.setStoreId(user.getStoreId());
            model.addAttribute("newZone", newZone);
            newZone.setCreatedAt(LocalDate.now());

            // Check if a zone with the same name already exists
            if (zoneService.isZoneNameAlreadyExists(newZone.getName(), user.getStoreId())) {
                model.addAttribute("errorMessage", "Zone with this name already exists");
                return "warehouse/create";
            }


            this.zoneService.handleSaveZone(newZone);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "warehouse/create";
        }
        return "redirect:/warehouse/listWarehouseZone";
    }

    @GetMapping("/zone")
    public String getZoneDetail(Model model, @RequestParam String id,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String searchBy,
                                @CookieValue(value = "token", required = false) String token) {
        Zone zone = this.zoneService.getZoneById(Long.parseLong(id));
        String role = jwtUtils.extractRole(token);
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        model.addAttribute("user", user.get());
        model.addAttribute("account", account.get());
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        model.addAttribute("zone", zone);
        model.addAttribute("id", id);

        // Fetch products by zone ID and filter by search query if provided
        List<Product> products;
        if (search != null && !search.isEmpty()) {
            if ("price".equals(searchBy)) {
                Double price  = Double.parseDouble(search);
                products = productService.searchProductsByZoneIdAndPrice(Long.parseLong(id),
                        price,price);
            } else {
                products = productService.searchProductsByZoneIdAndName(Long.parseLong(id), search);
            }
        } else {
            products = productService.getProductsByZoneId(Long.parseLong(id));
        }
        model.addAttribute("products", products);

        return "warehouse/zoneDetail";
    }

    @GetMapping("/zone/update")
    public String getUpdateZonePage(Model model, @RequestParam String id,
                                    @CookieValue(value = "token", required = false) String token) {
        Zone currentZone = this.zoneService.getZoneById(Long.parseLong(id));
        model.addAttribute("currentZone", currentZone);
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        model.addAttribute("user", user.get());
        model.addAttribute("account", account.get());
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        return "warehouse/updateZone";
    }

    @PostMapping("/zone/update")
    public String postUpdateZone(Model model, @ModelAttribute("currentZone") Zone zone,
                                 @CookieValue(value = "token", required = false) String token) {
        Zone currentZone = this.zoneService.getZoneById(zone.getId());
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        if (currentZone == null) {
            model.addAttribute("errorMessage", "Zone not found");
            return "warehouse/updateZone";
        }
        boolean isNameExists = this.zoneService.isZoneNameAlreadyExists(zone.getName(), zone.getId());
        if (isNameExists) {
            model.addAttribute("errorMessage", "Zone with this name already exists");
            model.addAttribute("currentZone", currentZone);
            return "warehouse/updateZone";
        }
        // update zone with new detail
        currentZone.setName(zone.getName());
        // Try saving the updated zone
        try {

            this.zoneService.updateZone(currentZone);
            this.zoneService.handleSaveZone(currentZone);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("currentZone", currentZone);
            return "warehouse/updateZone"; // Return to the update page with an error message
        }

        return "redirect:/warehouse/listWarehouseZone";
    }

    @GetMapping("/zone/delete")
    public String getDeleteZonePage(Model model, @RequestParam String id,
                                    @CookieValue(value = "token", required = false) String token) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        model.addAttribute("user", user.get());
        model.addAttribute("account", account.get());
        model.addAttribute("id", id);
        model.addAttribute("newZone", new Zone());
        return "warehouse/delete";

    }

    @PostMapping("/zone/delete")
    public String postDeleteZone(@RequestParam String id) {
        this.zoneService.deleteById(Long.parseLong(id));

        return "redirect:/warehouse/listWarehouseZone";
    }

    @GetMapping("/getZones")
    @ResponseBody
    public List<Zone> getZones(@CookieValue(value = "token", required = false) String token) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        List<Zone> zones = null;
        if (user.isPresent()) {
            zones = zoneService.getZonesByStoreId(user.get().getStoreId());
        }
        return zones;
    }

}