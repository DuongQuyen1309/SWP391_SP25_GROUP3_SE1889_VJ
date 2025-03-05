package com.demoproject.controller;

import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import com.demoproject.entity.Note;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.queuenotemoney.TransactionQueueProcessor;
import com.demoproject.queuenotemoney.TransactionRequest;
import com.demoproject.service.AccountService;
import com.demoproject.service.CustomerService;
import com.demoproject.service.NoteService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequestMapping("/note")
@Controller
public class NoteController {
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


    public NoteController(NoteService noteService, AccountService accountService,
                          UserService userService, TransactionQueueProcessor transactionQueueProcessor) {
        this.noteService = noteService;
        this.accountService = accountService;
        this.userService = userService;
        this.transactionQueueProcessor = transactionQueueProcessor;
    }

    @GetMapping("/listNote/{id}")
    public String listNote(Model model,
                           @PathVariable Long id,
                           @CookieValue(value = "token", required = false) String token,
                           @RequestParam(required = false) String idFrom,
                           @RequestParam(required = false) String idTo,
                           @RequestParam(required = false) String kindOfNote,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateFrom,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateTo,
                           @RequestParam(required = false) String noteSearch,
                           @RequestParam(required = false) String moneyFrom,
                           @RequestParam(required = false) String moneyTo,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        //khoi tao rong
        Page<Note> notePage ;

        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        model.addAttribute("account", account);

        Optional<Users> user = userService.getUserProfile(optAccount.get().getId());

        String role= jwtUtils.extractRole(token);
        model.addAttribute("role",role);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        List<Long> relatedUserList = new ArrayList<>();
        if(role.equalsIgnoreCase("OWNER")){
            relatedUserList = userService.getStaffID(user.get().getId());
            relatedUserList.add(user.get().getId());
        }else if(role.equalsIgnoreCase("STAFF")){
            Long ownerId = userService.getOwnerID(user.get().getId());
            relatedUserList = userService.getStaffID(ownerId);
            relatedUserList.add(ownerId);
        }

        if ((idFrom != null && !idFrom.isEmpty() && !idFrom.matches("\\d+")) ||
                (idTo != null && !idTo.isEmpty() && !idTo.matches("\\d+")) ||
                (moneyFrom != null && !moneyFrom.isEmpty() && !moneyFrom.matches("-?\\d+")) ||
                (moneyTo != null && !moneyTo.isEmpty() && !moneyTo.matches("-?\\d+"))) {

            model.addAttribute("errorMessage", "Id field should be positive number. Money field should be negative number");
            notePage = Page.empty();
            model.addAttribute("notes", notePage.getContent());

            Customer customer = customerService.getCustomer(id);
            model.addAttribute("customer", customer);
            model.addAttribute("idFrom", idFrom);
            model.addAttribute("idTo", idTo);
            model.addAttribute("kindOfNote", kindOfNote);
            model.addAttribute("createdDateFrom", createdDateFrom);
            model.addAttribute("createdDateTo", createdDateTo);
            model.addAttribute("noteSearch", noteSearch);
            model.addAttribute("moneyFrom", moneyFrom);
            model.addAttribute("moneyTo", moneyTo);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", notePage.getTotalPages());
            model.addAttribute("size", size);
            return "listNote";
        }
        else {


            if ((idFrom != null && !idFrom.isEmpty()) || (idTo != null && !idTo.isEmpty())
                    || (kindOfNote != null && !kindOfNote.isEmpty()) || (createdDateFrom != null)
                    || (createdDateTo != null) || (noteSearch != null && !noteSearch.isEmpty())
                    || (moneyFrom != null && !moneyFrom.isEmpty()) || (moneyTo != null && !moneyTo.isEmpty())) {


                Long req_idFrom = (idFrom != null && !idFrom.isBlank() && idFrom.matches("\\d+")) ? Long.valueOf(idFrom) : null;
                Long req_idTo = (idTo != null && !idTo.isBlank() && idTo.matches("\\d+")) ? Long.valueOf(idTo) : null;

                Integer req_moneyFrom = (moneyFrom != null && !moneyFrom.isBlank() && moneyFrom.matches("\\d+")) ? Integer.valueOf(moneyFrom) : null;
                Integer req_moneyTo = (moneyTo != null && !moneyTo.isBlank() && moneyTo.matches("\\d+")) ? Integer.valueOf(moneyTo) : null;

                String note_search = noteSearch == null || noteSearch.isEmpty() ? null : noteSearch;

                Boolean req_isDebt = (kindOfNote != null && !kindOfNote.isBlank()) ? Boolean.valueOf(kindOfNote) : null;
                notePage = noteService.searchNoteByAttribute(id, relatedUserList, req_idFrom, req_idTo, req_isDebt, createdDateFrom, createdDateTo, note_search,
                        req_moneyFrom, req_moneyTo, pageable);

            } else {
                notePage = noteService.searchNoteAll(id, relatedUserList, pageable);
            }

            Customer customer = customerService.getCustomer(id);
            model.addAttribute("customer", customer);
            model.addAttribute("notes", notePage.getContent());

            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", notePage.getTotalPages());
            model.addAttribute("size", size);

            model.addAttribute("idFrom", idFrom);
            model.addAttribute("idTo", idTo);
            model.addAttribute("kindOfNote", kindOfNote);
            model.addAttribute("createdDateFrom", createdDateFrom);
            model.addAttribute("createdDateTo", createdDateTo);
            model.addAttribute("noteSearch", noteSearch);
            model.addAttribute("moneyFrom", moneyFrom);
            model.addAttribute("moneyTo", moneyTo);

            return "listNote";
        }
    }

    @GetMapping("/createNote/{id}")
    public String createNoteForm(@PathVariable("id") Long id,
                             @CookieValue(value = "token", required = false) String token,
                             Model model) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        model.addAttribute("account", account);

        Optional<Users> user = userService.getUserProfile(optAccount.get().getId());
        String role= jwtUtils.extractRole(token);
        model.addAttribute("role",role);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        Customer customer = customerService.getCustomer(id);
        model.addAttribute("customer",customer);
        return "createNote";
    }

    @PostMapping("/createNote")
    public String createNote(@RequestParam("customerID") String customerID,
                             @RequestParam("notename") String notename,
                             @RequestParam("kindOfNote") String kindOfNote,
                             @RequestParam("money") String money,
                           Model model,@CookieValue(value = "token", required = false) String token) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        model.addAttribute("account", account);

        Optional<Users> user = userService.getUserProfile(optAccount.get().getId());
        String role= jwtUtils.extractRole(token);
        model.addAttribute("role",role);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        Long id = Long.parseLong(customerID);
        Integer amount = Integer.parseInt(money);
        Boolean isDebt = (kindOfNote != null && !kindOfNote.isBlank()) ? Boolean.valueOf(kindOfNote) : null;
        Long cID =  user.get().getId();
        TransactionRequest request = new TransactionRequest(id, amount, isDebt, notename,cID);
        transactionQueueProcessor.addTransaction(request);
        return "redirect:/note/listNote/" + customerID;

    }

}
