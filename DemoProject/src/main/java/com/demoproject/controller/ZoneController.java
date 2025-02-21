package com.demoproject.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.demoproject.entity.Account;
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
                             @RequestParam(defaultValue = "5") int size, @RequestParam(defaultValue = "") String search,
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

        if (user.get().getWarehouseName() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have any warehouse");
            return "redirect:/warehouse";
        }

        if (search.isEmpty()) {
            zonePage = this.zoneService.getAllZonesByWarehouseName(pageable, user.get().getWarehouseName());
        } else {
            zonePage = this.zoneService.getZonesByName(search, user.get().getWarehouseName(), pageable);
        }
        model.addAttribute("zonePage", zonePage);
        model.addAttribute("search", search);
        return "zone";
    }

    @GetMapping("/createWarehouseZone")
    public String getCreateZonePage(Model model,@CookieValue(value = "token", required = false) String token) {
        model.addAttribute("newZone", new Zone());
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        return "create";
    }

    @PostMapping("/createWarehouseZone")
    public String postMethodName(Model model, @ModelAttribute("newZone") Zone newZone,
                                 @CookieValue(value = "token", required = false) String token,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (!zoneService.existsById(newZone.getProductId())) {
                model.addAttribute("errorMessage", "Product ID does not exist");
                model.addAttribute("newZone", newZone);

                return "create";
            }
            String role= jwtUtils.extractRole(token);
            List<String> listHiddenPage = new ArrayList<>();
            listHiddenPage.add("");
            if(role.equals("STAFF")){
                listHiddenPage.add("listStaff");
            }
            model.addAttribute("listHiddenPage", listHiddenPage);
            String username = jwtUtils.extractUsername(token);
            Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
            Optional<Users> user = userService.getUserProfile(account.get().getUserId());
            newZone.setWarehouseName(user.get().getWarehouseName());
            model.addAttribute("newZone", newZone);
            newZone.setCreatedAt(LocalDate.now());
            this.zoneService.handleSaveZone(newZone);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "create";
        }
        return "redirect:/warehouse/listWarehouseZone";
    }

    @GetMapping("/zone/{id}")
    public String getZoneDetail(Model model, @PathVariable long id,@CookieValue(value = "token", required = false) String token) {
        Zone zone = this.zoneService.getZoneById(id);
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
        // HH:mm:ss");
        // String formattedCreatedAt = zone.getCreatedAt().format(formatter);
        model.addAttribute("zone", List.of(zone));
        // model.addAttribute("formattedCreatedAt", formattedCreatedAt);
        model.addAttribute("id", id);
        return "zoneDetail";

    }

    @GetMapping("/zone/update/{id}")
    public String getUpdateZonePage(Model model, @PathVariable long id,
                                    @CookieValue(value = "token", required = false) String token) {
        Zone currentZone = this.zoneService.getZoneById(id);
        model.addAttribute("currentZone", currentZone);
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        return "updateZone";
    }

    @PostMapping("/zone/update")
    public String postUpdateZone(Model model, @ModelAttribute("currentZone") Zone zone,
                                 @CookieValue(value = "token", required = false) String token) {
        Zone currentZone = this.zoneService.getZoneById(zone.getId());
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        if (currentZone == null) {
            model.addAttribute("errorMessage", "Zone not found");
            return "updateZone";
        }
        boolean isNameExists = this.zoneService.isZoneNameAlreadyExists(zone.getName(), zone.getId());
        if (isNameExists) {
            model.addAttribute("errorMessage", "Zone with this name already exists");
            model.addAttribute("currentZone", currentZone);
            return "updateZone";
        }
        // update zone with new detail
        currentZone.setName(zone.getName());
        currentZone.setWarehouseName(zone.getWarehouseName());
        currentZone.setProductId(zone.getProductId());
        currentZone.setAmount(zone.getAmount());
        // Try saving the updated zone
        try {
            if (!zoneService.existsById(currentZone.getProductId())) {
                model.addAttribute("errorMessage", "Product ID does not exist");
                model.addAttribute("currentZone", zone);
                return "updateZone";
            }
            this.zoneService.updateZone(currentZone);
            this.zoneService.handleSaveZone(currentZone);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("currentZone", currentZone);
            return "updateZone"; // Return to the update page with an error message
        }

        return "redirect:/warehouse/listWarehouseZone";
    }


    @GetMapping("/zone/delete/{id}")
    public String getDeleteZonePage(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        model.addAttribute("newZone", new Zone());
        return "delete";

    }

    @PostMapping("/zone/delete")
    public String postDeleteZone(@RequestParam("id") long id) {
        this.zoneService.deleteById(id);

        return "redirect:/warehouse/listWarehouseZone";
    }

}