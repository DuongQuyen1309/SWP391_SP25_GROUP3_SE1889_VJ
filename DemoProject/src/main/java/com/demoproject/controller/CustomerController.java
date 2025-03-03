package com.demoproject.controller;

import com.demoproject.dto.request.CustomerRequest;
import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.mapper.CustomerMapper;
import com.demoproject.service.AccountService;
import com.demoproject.service.CustomerService;
import com.demoproject.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public CustomerController(CustomerService customerService, UserService userService, AccountService accountService) {
        this.customerService = customerService;
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping("/listCustomer")
    public String listCustomer(Model model,
                               @CookieValue(value = "token", required = false) String token,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
//                               @RequestParam(required = false) String search,
//                               @RequestParam(required = false) String ctype,
                               @RequestParam(required = false) String idFrom,
                               @RequestParam(required = false) String idTo,
                               @RequestParam(required = false) String name,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dobFrom,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dobTo,
                               @RequestParam(required = false) String address,
                               @RequestParam(required = false) String phone,
                               @RequestParam(required = false) String moneyFrom,
                               @RequestParam(required = false) String moneyTo,
                               @RequestParam(required = false) String customerType,
                               RedirectAttributes redirectAttributes) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Customer> customerPage;
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        model.addAttribute("account", account);


        //lấy id của người đang đăng nhập
        Optional<Users> user = userService.getUserProfile(optAccount.get().getId());

        String role= jwtUtils.extractRole(token);
        model.addAttribute("role", role);
        List<Long> relatedUserList = new ArrayList<>();
        if(role.equalsIgnoreCase("OWNER")){
            relatedUserList = userService.getStaffID(user.get().getId());
            relatedUserList.add(user.get().getId());
            for(Long id:relatedUserList){
                System.out.println(id);
            }
        }else if(role.equalsIgnoreCase("STAFF")){
            Long ownerId = userService.getOwnerID(user.get().getId());
            relatedUserList = userService.getStaffID(ownerId);
            relatedUserList.add(ownerId);

        }
        List<String> phoneList =  customerService.getAllPhoneNumbers(relatedUserList);
        model.addAttribute("phoneList", phoneList);
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
            model.addAttribute("customerType", customerType);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", customerPage.getTotalPages());
            model.addAttribute("size", size);
            return "listCustomer";
        }
        else {
            if ((idFrom != null && !idFrom.isEmpty()) || (idTo != null && !idTo.isEmpty())
                    || (name != null && !name.isEmpty()) || (dobFrom != null)
                    || (dobTo != null) || (address != null && !address.isEmpty())
                    || (phone != null && !phone.isEmpty()) || (customerType != null && !customerType.isEmpty())
                    || (moneyFrom != null && !moneyFrom.isEmpty()) || (moneyTo != null && !moneyTo.isEmpty())) {


                Long req_idFrom = (idFrom != null && !idFrom.isBlank() && idFrom.matches("\\d+")) ? Long.valueOf(idFrom) : null;
                Long req_idTo = (idTo != null && !idTo.isBlank() && idTo.matches("\\d+")) ? Long.valueOf(idTo) : null;

                Integer req_moneyFrom = (moneyFrom != null && !moneyFrom.isBlank() && moneyFrom.matches("\\d+")) ? Integer.valueOf(moneyFrom) : null;
                Integer req_moneyTo = (moneyTo != null && !moneyTo.isBlank() && moneyTo.matches("\\d+")) ? Integer.valueOf(moneyTo) : null;

                String req_phone = (phone == null || phone.isEmpty())? null: phone;
                String req_customerType = (customerType == null || customerType.isEmpty())? null: customerType;
                String req_address = (address == null || address.isEmpty())? null: address;
                String req_name = (name == null || name.isEmpty()) ? null : name;

                customerPage = customerService.searchCustomerByAttribute(relatedUserList, req_idFrom, req_idTo, req_moneyFrom,
                        req_moneyTo, req_phone, dobFrom, dobTo, req_customerType, req_address, req_name, pageable);

            } else {
                customerPage = customerService.searchCustomerAll(relatedUserList, pageable);
            }

            model.addAttribute("customers", customerPage.getContent());

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
            model.addAttribute("customerType", customerType);
            model.addAttribute("moneyFrom", moneyFrom);
            model.addAttribute("moneyTo", moneyTo);

            return "listCustomer";
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
        model.addAttribute("account", account.get());
        String role= jwtUtils.extractRole(token);
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
        List<String> phoneList =  customerService.getAllPhoneNumbers(relatedUserList);
        model.addAttribute("phoneList", phoneList);
        return "createCustomer";
    }

    @PostMapping("/createCustomer")
    public String createCustomerList(
            @Valid @ModelAttribute("customerRequest") CustomerRequest customerRequest,
            BindingResult result,
            @RequestParam String lastname,
            @CookieValue(value = "token", required = false) String token,
            RedirectAttributes redirectAttributes,
            Model model) {

        String username = jwtUtils.extractUsername(token);
        Optional<Account> account = accountService.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> user = userService.getUserProfile(account.get().getUserId());
        model.addAttribute("account", account.get());
        String role= jwtUtils.extractRole(token);


        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);




        Long id = user.get().getId();


        String fullname = lastname.trim()+ " " +customerRequest.getName().trim();
        String phone = customerRequest.getPhone().trim();
        customerRequest.setPhone(phone.trim());
        customerRequest.setName(fullname);
        customerRequest.setMoneyState(0);
        customerRequest.setCreatedBy(id);
        customerRequest.setCreatedAt(LocalDate.now());

        try {
            customerService.createCustomer(customerRequest);
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Customer created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("messageType", "fail");
            redirectAttributes.addFlashAttribute("message", "Failed to create customer.");
        }

        return "redirect:/customer/listCustomer";
    }


    @GetMapping("/updateCustomer/{id}")
    public String getUpdatedCustomer(@PathVariable Long id, Model model,
                                     @CookieValue(value = "token", required = false) String token
    ) {
        Customer customer = customerService.getCustomer(id);
        String pre_phone = customer.getPhone();
        System.out.println("ngay sinh la " + customer.getId());
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        String role= jwtUtils.extractRole(token);
        List<Long> relatedUserList = new ArrayList<>();
        if(role.equalsIgnoreCase("OWNER")){
            relatedUserList = userService.getStaffID(user.get().getId());
            relatedUserList.add(user.get().getId());
        }else if(role.equalsIgnoreCase("STAFF")){
            Long ownerId = userService.getOwnerID(user.get().getId());
            relatedUserList = userService.getStaffID(ownerId);
            relatedUserList.add(ownerId);
        }
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        model.addAttribute("account", account);
        model.addAttribute("customer", customer);
        model.addAttribute("pre_phone", pre_phone);
        List<String> phoneList =  customerService.getAllPhoneNumbers(relatedUserList);
        model.addAttribute("phoneList", phoneList);
        return "updateCustomer";

    }


    @PostMapping("/updateCustomer")
    public String updateCustomer(
            @RequestParam String id,
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String dob,
            @RequestParam String phone,
            @RequestParam Boolean gender,
            @RequestParam String ctype,
            RedirectAttributes redirectAttributes,
            @CookieValue(value = "token", required = false) String token,
            Model model) {

        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        model.addAttribute("account", account);

        LocalDate dateOfBirth = LocalDate.parse(dob);

        // Tạo đối tượng Customer để cập nhật
        Customer customer = new Customer();
        customer.setId(Long.parseLong(id));
        customer.setName(name);
        customer.setAddress(address);
        customer.setPhone(phone);
        customer.setGender(gender);
        customer.setDob(dateOfBirth);
        customer.setCtype(ctype);
        customer.setUpdatedAt(LocalDate.now());

        // Lưu vào database
        customerService.updateCustomer(Long.parseLong(id), customer);

        redirectAttributes.addFlashAttribute("messageType", "success");
        redirectAttributes.addFlashAttribute("message", "Customer updated successfully!");

        return "redirect:/customer/listCustomer";
    }

}

