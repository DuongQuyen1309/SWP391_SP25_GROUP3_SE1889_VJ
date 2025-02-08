package com.demoproject.service;

import com.demoproject.entity.Account;
import com.demoproject.repository.AccountRepository;
import com.demoproject.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    public AccountService(AccountRepository accountRepository,UserRepository userRepository) {

        this.accountRepository = accountRepository;
        this.userRepository= userRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<Account> listOwnerAccount() {
        List<Long> ids=userRepository.findIdByRole("OWNER");
        List<Account> accounts= accountRepository.findByUserIdIn(ids);
        return accounts;
    }

}
