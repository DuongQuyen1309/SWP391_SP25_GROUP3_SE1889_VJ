package com.demoproject.controller;

import com.demoproject.entity.*;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        model.addAttribute("account", account);

        System.out.println(user.get().getId());
        model.addAttribute("user", user.orElse(null));

        String role= jwtUtils.extractRole(token);
        model.addAttribute("role",role);
        List<String> listHiddenPage = new ArrayList<>();
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Store");
            listHiddenPage.add("listOwner");
            listHiddenPage.add("Dashboard");
        }
        if(role.equals("OWNER")){
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

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


                // bắt đầu đoạn code moi
                notePage = noteService.searchNoteByAttribute(id,last_storedID, req_idFrom, req_idTo, req_isDebt, createdDateFrom, createdDateTo, note_search,
                        req_moneyFrom, req_moneyTo, pageable);
                // ket thuc đoạn code moi

            } else {

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
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        model.addAttribute("account", account);

        System.out.println(user.get().getId());
        model.addAttribute("user", user.orElse(null));
        String role= jwtUtils.extractRole(token);
        model.addAttribute("role",role);
        List<String> listHiddenPage = new ArrayList<>();
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Store");
            listHiddenPage.add("listOwner");
            listHiddenPage.add("Dashboard");
        }
        if(role.equals("OWNER")){
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        Customer customer = customerService.getCustomer(id);
        model.addAttribute("customer",customer);


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
                             @RequestParam("noteImage") MultipartFile noteImage,
                             RedirectAttributes redirectAttributes, HttpServletResponse response,
                             Model model,@CookieValue(value = "token", required = false) String token) {
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        model.addAttribute("account", account);

        System.out.println(user.get().getId());
        model.addAttribute("user", user.orElse(null));
        String role= jwtUtils.extractRole(token);
        model.addAttribute("role",role);
        List<String> listHiddenPage = new ArrayList<>();
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
            listHiddenPage.add("Store");
            listHiddenPage.add("listOwner");
            listHiddenPage.add("Dashboard");
        }
        if(role.equals("OWNER")){
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        List<Long> relatedUserList = new ArrayList<>();
        if (role.equalsIgnoreCase("OWNER")) {
            relatedUserList = userService.getStaffID(user.get().getId());
            relatedUserList.add(user.get().getId());
        } else if (role.equalsIgnoreCase("STAFF")) {
            Long ownerId = userService.getOwnerID(user.get().getId());
            relatedUserList = userService.getStaffID(ownerId);
            relatedUserList.add(ownerId);
        }

        Long tokenCustomerId = jwtUtils.extractCustomerId(token);
        if (tokenCustomerId == null || !tokenCustomerId.equals(id)) {
            return "redirect:/customer/listCustomer"; // Nếu không đúng, chặn request
        }else {

            String imagePath = null;
            if (!noteImage.isEmpty()) {
                try {
                    String uploadDir = "uploads/notes/";
                    Path uploadPath = Paths.get(uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    String fileName = UUID.randomUUID().toString().substring(0, 8) + "_" + noteImage.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(noteImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    imagePath = fileName;
                } catch (IOException e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("messageType", "fail");
                    redirectAttributes.addFlashAttribute("message", "Error uploading image!");
                    return "redirect:/note/createNote/" + id;
                }
            }

            Integer amount = Integer.parseInt(money);
            String isDebt = (kindOfNote != null && !kindOfNote.isBlank()) ? kindOfNote : null;
            Long cID = user.get().getId();
            String storeId = jwtUtils.extractStoreId(token);
            Long last_storedID = Long.parseLong(storeId);

            TransactionRequest request = new TransactionRequest(tokenCustomerId, amount, isDebt, notename, cID, last_storedID,imagePath);
            transactionQueueProcessor.addTransaction(request);
            redirectAttributes.addFlashAttribute("messageType", "success");
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
    @GetMapping("/image/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path imagePath = Paths.get("uploads/notes/").resolve(filename);
            Resource resource = new UrlResource(imagePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(imagePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
