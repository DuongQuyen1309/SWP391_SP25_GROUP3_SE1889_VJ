package com.demoproject.controller;

import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import com.demoproject.entity.CustomerUpdateLog;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.notemoneyqueue.TransactionQueueProcessor;
import com.demoproject.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/updateLog")
@Controller
public class UpdateLogController {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private NoteService noteService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private TransactionQueueProcessor transactionQueueProcessor;
    @Autowired
    private UpdateLogService updateLogService;

    public UpdateLogController(NoteService noteService, AccountService accountService,
                               UserService userService, TransactionQueueProcessor transactionQueueProcessor,
                               CustomerService customerService, UpdateLogService updateLogService) {
        this.noteService = noteService;
        this.accountService = accountService;
        this.userService = userService;
        this.transactionQueueProcessor = transactionQueueProcessor;
        this.customerService = customerService;
        this.updateLogService = updateLogService;
    }

    @GetMapping("/listUpdatedRecord")
    public String listUpdatedRecord(Model model,
                                    @CookieValue(value = "token", required = false) String token,
                                    @RequestParam(required = false) String updatedBy,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedDateFrom,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedDateTo,
                                    @RequestParam(required = false) String informationField,
                                    @RequestParam(required = false) String preValue,
                                    @RequestParam(required = false) String followValue,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<CustomerUpdateLog> updateLogPage;
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        model.addAttribute("account", account);
        model.addAttribute("user", user);

        Long ownerPreId = user.getId();
        model.addAttribute("ownerPreId", ownerPreId);

        List<Users> usersList = new ArrayList<>();
        String storeId = jwtUtils.extractStoreId(token);

        String role= jwtUtils.extractRole(token);

        model.addAttribute("role", role);


        List<Long> relatedUserList = new ArrayList<>();
        Long last_storedID = Long.parseLong(storeId);

        if(role.equalsIgnoreCase("OWNER")){
            relatedUserList = userService.getStaffID1(user.getId());
            relatedUserList.add(user.getId());
        }else if(role.equalsIgnoreCase("STAFF")){
            Long ownerId = userService.getOwnerID(user.getId());
            relatedUserList = userService.getStaffID1(ownerId);
            relatedUserList.add(ownerId);
        }

        List<String> listHiddenPage = new ArrayList<>();
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

        List<Customer> customerList = new ArrayList<>();
        if ((updatedDateFrom != null) || (updatedDateTo != null) || (updatedBy != null && !updatedBy.isEmpty())
                || (informationField != null && !informationField.isEmpty()) || (preValue != null && !preValue.isEmpty())
                || (followValue != null && !followValue.isEmpty()) || (status != null && !status.isEmpty())) {

            String req_informationField = (informationField == null || informationField.isEmpty()) ? null : informationField;
            Long req_updatedBy = (updatedBy != null && !updatedBy.isBlank()) ? Long.valueOf(updatedBy) : null;
            String req_preValue = (preValue == null || preValue.isEmpty()) ? null : preValue;
            String req_followValue = (followValue == null || followValue.isEmpty()) ? null : followValue;
            String req_status = (status == null || status.isEmpty()) ? null : status;


            updateLogPage = updateLogService.searchUpdateLogByAttribute(last_storedID,
                    req_informationField, req_updatedBy,
                    req_preValue, req_followValue,
                    updatedDateFrom, updatedDateTo, req_status, pageable);

            usersList = userService.getUsersInStore(last_storedID);
            customerList = customerService.getAllCustomerInStore(last_storedID);


        } else {
            updateLogPage = updateLogService.searchAllUpdateLog(last_storedID, pageable);
            usersList = userService.getUsersInStore(last_storedID);
            customerList = customerService.getAllCustomerInStore(last_storedID);

        }

        model.addAttribute("updateLog", updateLogPage.getContent());
        model.addAttribute("updatedDateFrom", updatedDateFrom);
        model.addAttribute("updatedDateTo", updatedDateTo);
        model.addAttribute("updatedBy", updatedBy);
        model.addAttribute("informationField", informationField);
        model.addAttribute("preValue", preValue);
        model.addAttribute("followValue", followValue);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", updateLogPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("usersList", usersList);
        model.addAttribute("customerList", customerList);

        return "customer/listUpdatedRecord";
    }
//    @GetMapping("/revert/{idLog}")
//    public String listUpdatedRecord(@PathVariable Long idLog, Model model) {
//        CustomerUpdateLog customerUpdateLog = updateLogService.getUpdateLogById(idLog);
//        if(customerUpdateLog != null){
//            Long customerId = customerUpdateLog.getCustomerId();
//            String fieldName = customerUpdateLog.getFieldName();
//            String oldValue = customerUpdateLog.getOldValue();
//            String newValue = customerUpdateLog.getNewValue();
//            UpdateLogService.revertRecord(customerId,fieldName,oldValue,)
//            return "listUpdatedRecord"
//        }else{
//            return "listUpdatedRecord";
//        }
//    }
}
