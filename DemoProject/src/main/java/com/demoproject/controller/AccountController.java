package com.demoproject.controller;


import com.demoproject.entity.Account;
import com.demoproject.entity.Student;
import com.demoproject.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/list")
    public String showForm(Model model) {
        model.addAttribute("username", new String()); // Khởi tạo đối tượng Student
        model.addAttribute("password", new String());
        model.addAttribute("displayname" , new String());
        List<Account> accounts= new ArrayList<Account>();
        accounts= accountService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        return "list"; // Tên file view: form.html
    }


    @PostMapping("/list")
    public String processSubmit(@ModelAttribute("firstName") String firstName, String lastName, String mark, Model model) {
        Account account = new Account();


        return "list"; // Chuyển hướng đến trang result.html
    }
}
