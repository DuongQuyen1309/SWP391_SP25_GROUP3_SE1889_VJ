package com.demoproject.controller;

import com.demoproject.dto.request.CustomerRequest;
import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.mapper.CustomerMapper;
import com.demoproject.queuenotemoney.TransactionQueueProcessor;
import com.demoproject.queuenotemoney.TransactionRequest;
import com.demoproject.service.AccountService;
import com.demoproject.service.CustomerService;
import com.demoproject.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
@RequestMapping("/customer")
@Controller
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private final UserService userService;
    @Autowired
    private final AccountService accountService;
    @Autowired
    private UserDetailsService userDetailsService; // Thêm Autowired để lấy thông tin user
    @Autowired
    private TransactionQueueProcessor transactionQueueProcessor;

    public CustomerController(CustomerService customerService, UserService userService,
                              AccountService accountService, TransactionQueueProcessor transactionQueueProcessor) {
        this.customerService = customerService;
        this.userService = userService;
        this.accountService = accountService;
        this.transactionQueueProcessor = transactionQueueProcessor;
    }

    @GetMapping("/listCustomer")
    public String listCustomer(Model model,
                               @CookieValue(value = "token", required = false) String token,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(required = false) String idFrom,
                               @RequestParam(required = false) String idTo,
                               @RequestParam(required = false) String name,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dobFrom,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dobTo,
                               @RequestParam(required = false) String address,
                               @RequestParam(required = false) String phone,
                               @RequestParam(required = false) String moneyFrom,
                               @RequestParam(required = false) String moneyTo,
                               @RequestParam(required = false) String createBy,
                               RedirectAttributes redirectAttributes) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Customer> customerPage;
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        model.addAttribute("account", account);
        model.addAttribute("user", user);


        List<Users> usersList = new ArrayList<>();
        String storeId = jwtUtils.extractStoreID(token);

        String role= jwtUtils.extractRole(token);

        model.addAttribute("role", role);


        List<Long> relatedUserList = new ArrayList<>();
        if(role.equalsIgnoreCase("OWNER")){
            relatedUserList = userService.getStaffID1(user.getId());
            relatedUserList.add(user.getId());
        }else if(role.equalsIgnoreCase("STAFF")){
            Long ownerId = userService.getOwnerID(user.getId());
            relatedUserList = userService.getStaffID1(ownerId);
            relatedUserList.add(ownerId);
        }


        //bắt đầu đoạn code thay thế//
        Long last_storedID = Long.parseLong(storeId);
        List<String> phoneList =  customerService.getAllPhones(last_storedID);
        model.addAttribute("phoneList", phoneList);
        //kết thúc đoạn code thay thế

        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        if ((idFrom != null && !idFrom.isEmpty() && !idFrom.matches("\\d+")) ||
                (idTo != null && !idTo.isEmpty() && !idTo.matches("\\d+")) ||
                (moneyFrom != null && !moneyFrom.isEmpty() && !moneyFrom.matches("-?\\d+")) ||
                (moneyTo != null && !moneyTo.isEmpty() && !moneyTo.matches("-?\\d+")) ||
                (phone != null && !phone.isEmpty() && !phone.matches("\\d+"))) {

            model.addAttribute("errorMessage", "Id, Phone fields should be positive number. Money field should be negative number");
            customerPage = Page.empty();
            model.addAttribute("customers", customerPage.getContent());

            model.addAttribute("idFrom", idFrom);
            model.addAttribute("idTo", idTo);
            model.addAttribute("name", name);
            model.addAttribute("dobFrom", dobFrom);
            model.addAttribute("dobTo", dobTo);
            model.addAttribute("address", address);
            model.addAttribute("phone", phone);
            model.addAttribute("moneyFrom", moneyFrom);
            model.addAttribute("moneyTo", moneyTo);
            model.addAttribute("createBy", createBy);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", customerPage.getTotalPages());
            model.addAttribute("size", size);
            return "customer/listCustomer";
        }
        else {
            if ((idFrom != null && !idFrom.isEmpty()) || (idTo != null && !idTo.isEmpty())
                    || (name != null && !name.isEmpty()) || (dobFrom != null)
                    || (dobTo != null) || (address != null && !address.isEmpty())
                    || (phone != null && !phone.isEmpty()) || (createBy != null && !createBy.isEmpty())
                    || (moneyFrom != null && !moneyFrom.isEmpty()) || (moneyTo != null && !moneyTo.isEmpty())) {


                Long req_idFrom = (idFrom != null && !idFrom.isBlank() && idFrom.matches("\\d+")) ? Long.valueOf(idFrom) : null;
                Long req_idTo = (idTo != null && !idTo.isBlank() && idTo.matches("\\d+")) ? Long.valueOf(idTo) : null;

                Integer req_moneyFrom = (moneyFrom != null && !moneyFrom.isBlank() && moneyFrom.matches("\\d+")) ? Integer.valueOf(moneyFrom) : null;
                Integer req_moneyTo = (moneyTo != null && !moneyTo.isBlank() && moneyTo.matches("\\d+")) ? Integer.valueOf(moneyTo) : null;

                String req_phone = (phone == null || phone.isEmpty())? null: phone;
                Long req_createBy = (createBy != null && !createBy.isBlank()) ? Long.valueOf(createBy) : null;
                String req_address = (address == null || address.isEmpty())? null: address;
                String req_name = (name == null || name.isEmpty()) ? null : name;


                // bắt đầu đoạn code moi
                customerPage = customerService.searchCustomerByAttribute(last_storedID, req_idFrom, req_idTo, req_moneyFrom,
                        req_moneyTo, req_phone, dobFrom, dobTo, req_createBy, req_address, req_name, pageable);
                // kết thúc đoạn code moi

                usersList = userService.getUsers(relatedUserList);


            } else {

                // bắt đầu đoạn code moi
                customerPage = customerService.searchCustomerAll(last_storedID, pageable);
                //kết thúc đoạn code moi

                usersList = userService.getUsers(relatedUserList);

            }

            model.addAttribute("customers", customerPage.getContent());
            model.addAttribute("usersList", usersList);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", customerPage.getTotalPages());
            model.addAttribute("size", size);

            model.addAttribute("idFrom", idFrom);
            model.addAttribute("idTo", idTo);
            model.addAttribute("name", name);
            model.addAttribute("dobFrom", dobFrom);
            model.addAttribute("dobTo", dobTo);
            model.addAttribute("address", address);
            model.addAttribute("phone", phone);
            model.addAttribute("createBy", createBy);
            model.addAttribute("moneyFrom", moneyFrom);
            model.addAttribute("moneyTo", moneyTo);

            return "customer/listCustomer";
        }
    }


    @GetMapping("/createCustomer")
    public String createCustomer(Model model,
                                 @CookieValue(value = "token", required = false) String token) {
        if (!model.containsAttribute("customerRequest")) {
            model.addAttribute("customerRequest", new CustomerRequest());
        }
        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        model.addAttribute("user", user.get());
        model.addAttribute("account", account.get());

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

        //bắt đầu đoạn code thay thế//
        String storeId = jwtUtils.extractStoreID(token);
        Long last_storedID = Long.parseLong(storeId);
        List<String> phoneList =  customerService.getAllPhones(last_storedID);
        model.addAttribute("phoneList", phoneList);
        //kết thúc đoạn code thay thế

        return "customer/createCustomer";
    }

    @PostMapping("/createCustomer")
    public String createCustomerList(
            @Valid @ModelAttribute("customerRequest") CustomerRequest customerRequest,
            BindingResult result,
            @CookieValue(value = "token", required = false) String token,
            RedirectAttributes redirectAttributes,
            @RequestParam("kindOfNote") String kindOfNote, @RequestParam("noteName") String noteName,
            Model model) {

        String username = jwtUtils.extractUsername(token);
        Account account = accountService.findByUsernameAndIsDeleteFalse(username).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        String role= jwtUtils.extractRole(token);


        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        String storeId = jwtUtils.extractStoreID(token);
        Long last_storedID = Long.parseLong(storeId);



        Long id = user.getId();
        String phone = customerRequest.getPhone().trim();
        customerRequest.setPhone(phone.trim());
        customerRequest.setCreatedBy(id);
        Integer moneyInNote = customerRequest.getMoneyState();
        customerRequest.setMoneyState(0);
        customerRequest.setCreatedAt(LocalDate.now());
        customerRequest.setStoreId(user.getStoreId());


        try {
            customerService.createCustomer(customerRequest);
            if((!kindOfNote.isBlank() ) && (!noteName.isEmpty()) && (moneyInNote != null && moneyInNote != 0)) {
                Customer savedCustomer = customerService.searchCustomer(customerRequest.getPhone());
                TransactionRequest request = new TransactionRequest(savedCustomer.getId(), moneyInNote
                        , kindOfNote, noteName, user.getId(), last_storedID);
                transactionQueueProcessor.addTransaction(request);
            }
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Customer created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("messageType", "fail");
            redirectAttributes.addFlashAttribute("message", "Failed to create customer.");
        }

        return "redirect:/customer/listCustomer";
    }


    @GetMapping("/updateCustomer/{id}")
    public String getUpdatedCustomer(Model model,
                                     @PathVariable Long id,
                                     @CookieValue(value = "token", required = false) String token, HttpServletResponse response
    ) {
            Customer customer = customerService.getCustomer(id);
            String pre_phone = customer.getPhone();

            String username = jwtUtils.extractUsername(token);
            Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
            Account account = optAccount.orElse(null);
            Optional<Users> user = userService.getUserProfile(account.getUserId());
            model.addAttribute("user", user.get());
            String role = jwtUtils.extractRole(token);

            // bắt đầu đoạn code moi
            String storeId = jwtUtils.extractStoreID(token);
            Long last_storedID = Long.parseLong(storeId);
            Long createdByUserID = customer.getCreatedBy();
            Optional<Users> createdByUser = userService.getUserProfile(createdByUserID);
            if(!last_storedID.equals(createdByUser.get().getStoreId())) {
                return "redirect:/customer/listCustomer";
            }
            // ket thuc đoạn code moi

            String updatedToken = jwtUtils.addCustomerIdToToken(token, id);

            // Cập nhật cookie với token mới
            Cookie cookie = new Cookie("token", updatedToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

                List<String> listHiddenPage = new ArrayList<>();
                if (role.equals("STAFF")) {
                    listHiddenPage.add("listStaff");
                }
                model.addAttribute("listHiddenPage", listHiddenPage);
                model.addAttribute("account", account);
                model.addAttribute("customer", customer);
                model.addAttribute("pre_phone", pre_phone);


            //bắt đầu đoạn code thay thế//
            List<String> phoneList =  customerService.getAllPhones(last_storedID);
            model.addAttribute("phoneList", phoneList);
            //kết thúc đoạn code thay thế

                return "customer/updateCustomer";


    }


    @PostMapping("/updateCustomer/{id}")
    public String updateCustomer(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String dob,
            @RequestParam String phone,
            @RequestParam Boolean gender,
            RedirectAttributes redirectAttributes,
            @CookieValue(value = "token", required = false) String token,HttpServletResponse response,
            Model model) {

        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        model.addAttribute("user", user.get());
        model.addAttribute("account", account);

        LocalDate dateOfBirth = LocalDate.parse(dob);

        // Tạo đối tượng Customer để cập nhật
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setAddress(address);
        customer.setPhone(phone);
        customer.setGender(gender);
        customer.setDob(dateOfBirth);
        customer.setUpdatedAt(LocalDate.now());

        Long tokenCustomerId = jwtUtils.extractCustomerId(token);
        if (tokenCustomerId == null || !tokenCustomerId.equals(id)) {
            return "redirect:/customer/listCustomer"; // Nếu không đúng, chặn request
        }
        String storeId = jwtUtils.extractStoreID(token);
        Long last_storedID = Long.parseLong(storeId);
        String role = jwtUtils.extractRole(token);
        String status;
        if(role.equals("STAFF")) {
            status = "Pending";
        }else {
            status = "Approved";
        }
        // Lưu vào database
        customerService.updateCustomer(id, customer,user.get().getId(), status, last_storedID);

        redirectAttributes.addFlashAttribute("messageType", "success");
        redirectAttributes.addFlashAttribute("message", "Customer updated successfully!");
        // Xóa customerId khỏi token sau khi tạo phiếu
        String updatedToken = jwtUtils.removeCustomerIdFromToken(token);
        Cookie cookie = new Cookie("token", updatedToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/customer/listCustomer";
    }

}