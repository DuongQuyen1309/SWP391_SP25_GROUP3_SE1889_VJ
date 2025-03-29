package com.demoproject.controller;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Changed this import
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.demoproject.entity.Account;
import com.demoproject.entity.Users;
import com.demoproject.entity.Zone;
import com.demoproject.entity.Package; // Make sure this import exists
import com.demoproject.jwt.JwtUtils;
import com.demoproject.service.AccountService;
import com.demoproject.service.PackageService;
import com.demoproject.service.UserService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/package")
public class PackageController {
    private PackageService packageService;
    private final AccountService accountService;
    private final UserService userService;
    @Autowired
    private JwtUtils jwtUtils;

    public PackageController(PackageService packageService, AccountService accountService, UserService userService) {
        this.packageService = packageService;
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping("/listPackage")
    public String getAllPackage(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(required = false) String idFrom,
                                @RequestParam(required = false) String idTo,
                                @RequestParam(required = false) String packageName,
                                @RequestParam(required = false) String color,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                @CookieValue(value = "token", required = false) String token,
                                RedirectAttributes redirectAttributes) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Package> packagePage;

        Account account = accountService.getAccountFromToken(token).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");

        if (role.equals("STAFF")) {
            listHiddenPage.add("Store");
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
            listHiddenPage.add("createPackage");
            listHiddenPage.add("deletePackage");
        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        // Thêm các giá trị filter vào model
        model.addAttribute("idFrom", idFrom);
        model.addAttribute("idTo", idTo);
        model.addAttribute("packageName", packageName);
        model.addAttribute("color", color);
        model.addAttribute("description", description);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Xử lý filter
        Long fromId = null;
        Long toId = null;
        LocalDate start = null;
        LocalDate end = null;

        try {
            if (idFrom != null && !idFrom.trim().isEmpty())
                fromId = Long.parseLong(idFrom);
            if (idTo != null && !idTo.trim().isEmpty())
                toId = Long.parseLong(idTo);
            if (startDate != null && !startDate.trim().isEmpty())
                start = LocalDate.parse(startDate);
            if (endDate != null && !endDate.trim().isEmpty())
                end = LocalDate.parse(endDate);
        } catch (Exception e) {
            model.addAttribute("error", "Invalid filter input: " + e.getMessage());
            packagePage = packageService.getAllPackageByStoreId(pageable, user.getStoreId());
        }

        if (fromId != null || toId != null || packageName != null || color != null || description != null
                || start != null || end != null) {
            packagePage = packageService.getPackagesWithFilters(fromId, toId, packageName, color, description, start,
                    end, user.getStoreId(), pageable);
        } else {
            packagePage = packageService.getAllPackageByStoreId(pageable, user.getStoreId());
        }

        model.addAttribute("packagePage", packagePage);
        return "package/listPackage";
    }

    @GetMapping("/createPackage")
    public String getCreatePackage(Model model, @CookieValue(value = "token", required = false) String token) {
        model.addAttribute("newPackage", new Package());
        String role = jwtUtils.extractRole(token);
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        model.addAttribute("user", user.get());
        model.addAttribute("account", account.get());
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("Store");
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
        }
        if(role.equals("OWNER")){
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        return "package/createPackage";
    }

    @PostMapping("/createPackage")
    public String postCreatePackage(Model model, @ModelAttribute("newPackage") Package newPackage,

                                    @CookieValue(value = "token", required = false) String token,
                                    RedirectAttributes redirectAttributes) {
        try {
            String role = jwtUtils.extractRole(token);
            List<String> listHiddenPage = new ArrayList<>();
            listHiddenPage.add("");
            if (role.equals("STAFF")) {
                listHiddenPage.add("listStaff");
                listHiddenPage.add("Dashboard");
                listHiddenPage.add("Store");
                listHiddenPage.add("listOwner");
            }
            if (role.equals("OWNER")) {
                listHiddenPage.add("listOwner");
            }
            model.addAttribute("listHiddenPage", listHiddenPage);
            String username = jwtUtils.extractUsername(token);
            Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
            Users user = userService.getUserProfile(account.get().getUserId()).orElse(null);
            model.addAttribute("user", user);
            newPackage.setStoreId(user.getStoreId());
            newPackage.setCreatedBy(user.getId());
            model.addAttribute("newPackage", newPackage);
            newPackage.setCreatedAt(LocalDate.now());


            if (packageService.isPackageNameAlreadyExists(newPackage.getName(), user.getStoreId())) {
                model.addAttribute("errorMessage", "Package with this name already exists");
                return "package/createPackage";
            }

            this.packageService.handldeSavePackage(newPackage);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "package/createPackage";
        }

        return "redirect:/package/listPackage";
    }

    @PostMapping("/deletePackage")
    public String deletePackage(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        packageService.deletePackageById(id);
        redirectAttributes.addFlashAttribute("message", "Package deleted successfully");
        return "redirect:/package/listPackage";
    }

    @GetMapping("/editPackage")
    public String getEditPackage(Model model, @RequestParam String id,
                                 @CookieValue(value = "token", required = false) String token) {
        Package currentPackage = this.packageService.getPackageById(Long.parseLong(id));
        // Lấy thông tin người dùng từ token
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("Store");
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        model.addAttribute("currentPackage", currentPackage);
        model.addAttribute("preName",currentPackage.getName());
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
        }

        return "package/editPackage";
    }

    @PostMapping("/editPackage")
    public String postUpdateZone(Model model, @ModelAttribute("currentPackage") Package package1,
                                 @CookieValue(value = "token", required = false) String token) {
        Package currentPackage = this.packageService.getPackageById(package1.getId());
        // Lấy thông tin người dùng từ token
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("Store");
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        if (currentPackage == null) {
            model.addAttribute("errorMessage", "Package not found");
            return "package/editPackage";
        }
        boolean isNameExists = this.packageService.isPackageNameAlreadyExists(package1.getName(), package1.getId());
        if (isNameExists) {
            model.addAttribute("errorMessage", "Package with this name already exists");
            model.addAttribute("currentPackage", currentPackage);
            return "package/editPackage";
        }
        // update zone with new detail
        currentPackage.setName(package1.getName());
        currentPackage.setDescription(package1.getDescription());
        currentPackage.setUpdatedAt(LocalDate.now());
        currentPackage.setUpdatedBy(user.getId());
        try {

            this.packageService.updatePackage(currentPackage);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("currentPackage", currentPackage);
            return "package/editPackage"; // Return to the update page with an error message
        }

        return "redirect:/package/listPackage";
    }

}