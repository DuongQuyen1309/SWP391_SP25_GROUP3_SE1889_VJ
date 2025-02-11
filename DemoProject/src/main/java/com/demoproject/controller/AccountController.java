package com.demoproject.controller;

import com.demoproject.entity.User;
import com.demoproject.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff-accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @GetMapping
    public String listStaffAccounts(Model model) {
        model.addAttribute("accounts", accountService.getStaffAccounts());
        return "staff_accounts";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", accountService.getUserById(id));
        return "edit_user";
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        accountService.updateUser(user);
        return ResponseEntity.ok("User updated successfully");
    }



}
