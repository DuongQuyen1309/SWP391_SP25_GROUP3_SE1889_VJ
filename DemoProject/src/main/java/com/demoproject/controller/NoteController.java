package com.demoproject.controller;

import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import com.demoproject.entity.Note;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.notemoneyqueue.TransactionQueueProcessor;
import com.demoproject.notemoneyqueue.TransactionRequest;
import com.demoproject.service.AccountService;
import com.demoproject.service.CustomerService;
import com.demoproject.service.NoteService;
import com.demoproject.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
                          UserService userService, TransactionQueueProcessor transactionQueueProcessor,CustomerService customerService) {
        this.noteService = noteService;
        this.accountService = accountService;
        this.userService = userService;
        this.transactionQueueProcessor = transactionQueueProcessor;
        this.customerService = customerService;
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

        Account account = accountService.getAccountFromToken(token).orElse(null);
        model.addAttribute("account", account);


        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        model.addAttribute("user", user);

        String role= jwtUtils.extractRole(token);
        model.addAttribute("role",role);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        // bắt đầu đoạn code cần thay thế
//        List<Long> relatedUserList = new ArrayList<>();
//        if(role.equalsIgnoreCase("OWNER")){
//            relatedUserList = userService.getStaffID1(user.get().getId());
//            relatedUserList.add(user.get().getId());
//        }else if(role.equalsIgnoreCase("STAFF")){
//            Long ownerId = userService.getOwnerID(user.get().getId());
//            relatedUserList = userService.getStaffID1(ownerId);
//            relatedUserList.add(ownerId);
//        }
//
//        Customer customercCheck = customerService.getCustomer(id);
//
//        int check=0;
//        for(int i=0; i<relatedUserList.size(); i++){
//            if(relatedUserList.get(i).equals(customercCheck.getCreatedBy())){
//                check=check+1;
//            }
//        }
//        if(check==0) {
//            return "redirect:/customer/listCustomer";
//        }
        //ket thuc doan code can thay the

        // bắt đầu đoạn code moi
        String storeId = jwtUtils.extractStoreId(token);
        Long last_storedID = Long.parseLong(storeId);
        Customer customerForNote = customerService.getCustomer(id);
        Long createdByUserID = customerForNote.getCreatedBy();
        Optional<Users> createdByUser = userService.getUserProfile(createdByUserID);
        if(!last_storedID.equals(createdByUser.get().getStoreId())) {
            return "redirect:/customer/listCustomer";
        }
        // ket thuc đoạn code moi

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
            return "note/listNote";
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

                String req_isDebt = (kindOfNote != null && !kindOfNote.isBlank()) ? kindOfNote : null;

                // bắt đầu đoạn code cần thay thế
//                notePage = noteService.searchNoteByAttribute(id, relatedUserList, req_idFrom, req_idTo, req_isDebt, createdDateFrom, createdDateTo, note_search,
//                        req_moneyFrom, req_moneyTo, pageable);
                // kết thúc đoạn code cần thay thế

                // bắt đầu đoạn code moi
                notePage = noteService.searchNoteByAttribute(id,last_storedID, req_idFrom, req_idTo, req_isDebt, createdDateFrom, createdDateTo, note_search,
                        req_moneyFrom, req_moneyTo, pageable);
                // ket thuc đoạn code moi

            } else {
                // bắt đầu đoạn code cần thay thế
//                notePage = noteService.searchNoteAll(id, relatedUserList, pageable);
                // ket thuc đoạn code cần thay thế

                // bắt đầu đoạn code moi
                notePage = noteService.searchNoteAll(id, last_storedID, pageable);
                // ket thuc đoạn code moi
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

            return "note/listNote";
        }
    }

    @GetMapping("/createNote/{id}")
    public String createNoteForm(@PathVariable("id") Long id,
                                 @CookieValue(value = "token", required = false) String token,
                                 Model model,  HttpServletResponse response) {
        Account account = accountService.getAccountFromToken(token).orElse(null);
        model.addAttribute("account", account);

        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        model.addAttribute("user", user);
        String role= jwtUtils.extractRole(token);
        model.addAttribute("role",role);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        Customer customer = customerService.getCustomer(id);
        model.addAttribute("customer",customer);

        //bat dau doan code can thay the
//        List<Long> relatedUserList = new ArrayList<>();
//        if(role.equalsIgnoreCase("OWNER")){
//            relatedUserList = userService.getStaffID1(user.get().getId());
//            relatedUserList.add(user.get().getId());
//        }else if(role.equalsIgnoreCase("STAFF")){
//            Long ownerId = userService.getOwnerID(user.get().getId());
//            relatedUserList = userService.getStaffID1(ownerId);
//            relatedUserList.add(ownerId);
//        }
//
//        Customer customercCheck = customerService.getCustomer(id);
//
//        int check=0;
//        for(int i=0; i<relatedUserList.size(); i++){
//            if(relatedUserList.get(i).equals(customercCheck.getCreatedBy())){
//                check=check+1;
//            }
//        }
//        if(check==0) {
//            return "redirect:/customer/listCustomer";
//        }
        //ket thuc doan code can thay the

        //bắt đầu đoạn code mới
        String storeId = jwtUtils.extractStoreId(token);
        Long last_storedID = Long.parseLong(storeId);
        Long createdByUserID = customer.getCreatedBy();
        Optional<Users> createdByUser = userService.getUserProfile(createdByUserID);
        if(!last_storedID.equals(createdByUser.get().getStoreId())) {
            return "redirect:/customer/listCustomer";
        }
        //ket thuc doan code moi

        String updatedToken = jwtUtils.addCustomerIdToToken(token, id);

        // Cập nhật cookie với token mới
        Cookie cookie = new Cookie("token", updatedToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);


        model.addAttribute("customerId", id);

        return "note/createNote";
    }

    @PostMapping("/createNote/{id}")
    public String createNote(@PathVariable("id") Long id,
                             @RequestParam("notename") String notename,
                             @RequestParam("kindOfNote") String kindOfNote,
                             @RequestParam("money") String money,
                             RedirectAttributes redirectAttributes, HttpServletResponse response,
                             Model model,@CookieValue(value = "token", required = false) String token) {
        Account account = accountService.getAccountFromToken(token).orElse(null);
        model.addAttribute("account", account);

        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        model.addAttribute("user", user);
        String role= jwtUtils.extractRole(token);
        model.addAttribute("role",role);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        List<Long> relatedUserList = new ArrayList<>();
        if (role.equalsIgnoreCase("OWNER")) {
            relatedUserList = userService.getStaffID(user.getId());
            relatedUserList.add(user.getId());
        } else if (role.equalsIgnoreCase("STAFF")) {
            Long ownerId = userService.getOwnerID(user.getId());
            relatedUserList = userService.getStaffID(ownerId);
            relatedUserList.add(ownerId);
        }

        Long tokenCustomerId = jwtUtils.extractCustomerId(token);
        if (tokenCustomerId == null || !tokenCustomerId.equals(id)) {
            return "redirect:/customer/listCustomer"; // Nếu không đúng, chặn request
        }else {

            Integer amount = Integer.parseInt(money);
            String isDebt = (kindOfNote != null && !kindOfNote.isBlank()) ? kindOfNote : null;
            Long cID = user.getId();
            String storeId = jwtUtils.extractStoreId(token);
            Long last_storedID = Long.parseLong(storeId);

            TransactionRequest request = new TransactionRequest(tokenCustomerId, amount, isDebt, notename, cID, last_storedID);
            transactionQueueProcessor.addTransaction(request);
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Note created successfully!");
            model.addAttribute("customerId", id);
            // Xóa customerId khỏi token sau khi tạo phiếu
            String updatedToken = jwtUtils.removeCustomerIdFromToken(token);
            Cookie cookie = new Cookie("token", updatedToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);

            return "redirect:/note/listNote/" + id;
        }

    }

}
