package com.demoproject.controller;

import com.demoproject.dto.response.ImportedNoteDetailResponse;
import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import com.demoproject.entity.ImportedNote;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.CustomerRepository;
import com.demoproject.repository.ProductRepository;
import com.demoproject.repository.ZoneRepository;
import com.demoproject.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/importednote")
@RequiredArgsConstructor
public class ImportedNoteController {
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AccountService accountService;
    private final ImportedNoteService importedNoteService;
    private final CustomerService customerService;
    private final String ACCOUNT = "account", STAFF = "STAFF", LISTSTAFF = "listStaff", LISTHIDDENPAGE = "listHiddenPage";


    @GetMapping("/list")
    public String showListProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String idFrom,
            @RequestParam(required = false) String idTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String moneyFrom,
            @RequestParam(required = false) String moneyTo,
            @RequestParam(required = false) String supplier,
            @CookieValue(value = "token", required = false) String token,
            Model model) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        Optional<Users> optUser = userService.getUserProfile(account.getUserId());
        model.addAttribute("user", optUser.get());
        model.addAttribute("account", account);
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("Store");
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");

        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        Page<ImportedNote> importedNotePage = importedNoteService.findImportedNotesWithFilters(
                page, size, sortField, sortDirection, idFrom, idTo, dateFrom, dateTo, moneyFrom, moneyTo, supplier);

        System.out.println(importedNotePage);

//        Page<ImportedNote> importedNotePage = importedNoteService.getImportedNotes(optUser.get().getStoreId(), page, size, sortField, sortDirection);
        model.addAttribute("account", account);
        model.addAttribute("importedNotes", importedNotePage.stream().toList());
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", importedNotePage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");

        model.addAttribute("idFrom", idFrom);
        model.addAttribute("idTo", idTo);
        model.addAttribute("moneyFrom", moneyFrom);
        model.addAttribute("moneyTo", moneyTo);
        model.addAttribute("supplier", supplier);


        return "importednote/listImportedNote";
    }

    @GetMapping("/list/{id}")
    @ResponseBody
    public ResponseEntity<ImportedNoteDetailResponse> getImportNoteDetails(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable("id") Long id,
            Model model) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        Optional<Users> optUser = userService.getUserProfile(account.getUserId());
        model.addAttribute("user", optUser.get());
        model.addAttribute("account", account);
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("Store");
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Dashboard");
            listHiddenPage.add("listOwner");
        }
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        ImportedNoteDetailResponse importNote = importedNoteService.getImportedNoteById(id);
        if (importNote == null) {
            return ResponseEntity.notFound().build(); // Return 404 if no data is found
        }

        System.out.println(importNote.getCustomerName());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(importNote);
    }

}
