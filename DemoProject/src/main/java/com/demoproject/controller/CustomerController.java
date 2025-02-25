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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String ctype,
                               RedirectAttributes redirectAttributes) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Customer> customerPage;
        String username = jwtUtils.extractUsername(token);
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());

        model.addAttribute("account", account);
        model.addAttribute("user", user.orElse(null));

        List<String> phoneList =  customerService.getAllPhoneNumbers();
        model.addAttribute("phoneList", phoneList);
        System.out.println("phonne: ");
        for(String phone : phoneList) {
            System.out.println(phone);
        }
        //lấy id của người đang đăng nhập
        System.out.println("user nguoi dang nhap: "+ user.get().getId());

        String role= jwtUtils.extractRole(token);
        System.out.println("role la "+ role);
        List<Long> relatedUserList = new ArrayList<>();
        if(role.equalsIgnoreCase("OWNER")){
            relatedUserList = userService.getStaffID(user.get().getId());
            relatedUserList.add(user.get().getId());
            System.out.println("cac id cau user "+user.get().getId()+" la:");
            for(Long id:relatedUserList){
                System.out.println(id);
            }
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

        if ((search != null && !search.isEmpty()) || (ctype != null && !ctype.isEmpty())) {
            customerPage = customerService.searchCustomersByNameAndType(relatedUserList,search, ctype, pageable);
        } else {
            customerPage = customerService.getAllCustomersAndIsDeleteFalse(relatedUserList,pageable);
        }

        List<String> customerTypes = customerService.getAllCustomerTypes();

        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("ctype", ctype);  // Đảm bảo giá trị ctype được truyền vào Thymeleaf
        model.addAttribute("customerTypes", customerTypes);

        return "customer/listCustomer";
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
        model.addAttribute("user", user.orElse(null));
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        return "customer/createCustomer";
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
        model.addAttribute("user", user.orElse(null));
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        if (result.hasErrors() &&(!lastname.matches("^[a-zA-ZÀ-Ỹà-ỹ\\s]+$") || lastname.trim().isEmpty())) {
            model.addAttribute("customerRequest", customerRequest);
            model.addAttribute("lastnamemessage","Not be empty, not contain number and special characters");
            return "customer/createCustomer"; // Quay lại form nếu có lỗi
        }

        if (result.hasErrors() ) {
            model.addAttribute("customerRequest", customerRequest);
            model.addAttribute("lastname", lastname);
            return "customer/createCustomer"; // Quay lại form nếu có lỗi
        }
        if (!lastname.matches("^[a-zA-ZÀ-Ỹà-ỹ\\s]+$") || lastname.trim().isEmpty()) {
            model.addAttribute("customerRequest", customerRequest);
            model.addAttribute("lastnamemessage","Not be empty, not contain number and special characters");
            return "customer/createCustomer"; // Quay lại form nếu có lỗi
        }
        if (customerService.isPhoneNumberExist(customerRequest.getPhone())) {
            model.addAttribute("lastname", lastname);
            model.addAttribute("customerRequest", customerRequest);
            model.addAttribute("phoneError","Phone is not duplicatied in system");
            return "customer/createCustomer"; // Quay lại form nếu có lỗi
        }


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
        System.out.println("ngay sinh la " + customer.getId());
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        Account account = optAccount.orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        model.addAttribute("user", user.orElse(null));
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        model.addAttribute("account", account);
        model.addAttribute("customer", customer);
        List<String> phoneList =  customerService.getAllPhoneNumbers();
        model.addAttribute("phoneList", phoneList);
        return "customer/updateCustomer";

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
        Optional<Users> user = userService.getUserProfile(account.getUserId());
        model.addAttribute("user", user.orElse(null));
        model.addAttribute("account", account);


        // Kiểm tra Name
        if (name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("messageType", "fail");
            redirectAttributes.addFlashAttribute("message", "Name cannot be empty.");
            return "redirect:/customer/updateCustomer/" + id;
        }

        // Kiểm tra Address
        if (address.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("messageType", "fail");
            redirectAttributes.addFlashAttribute("message", "Address cannot be empty.");
            return "redirect:/customer/updateCustomer/" + id;
        }

        // Kiểm tra Date of Birth
        LocalDate dateOfBirth;
        try {
            dateOfBirth = LocalDate.parse(dob);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("messageType", "fail");
            redirectAttributes.addFlashAttribute("message", "Invalid date format. Please use yyyy-MM-dd.");
            return "redirect:/customer/updateCustomer/" + id;
        }

        if (dateOfBirth.isAfter(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("messageType", "fail");
            redirectAttributes.addFlashAttribute("message", "Date of Birth cannot be a future date.");
            return "redirect:/customer/updateCustomer/" + id;
        }

        // Kiểm tra Phone
        if (!phone.matches("^[0-9]{10,11}$")) {
            redirectAttributes.addFlashAttribute("messageType", "fail");
            redirectAttributes.addFlashAttribute("message", "Phone number must be between 10 and 11 digits and contain only numbers.");
            return "redirect:/customer/updateCustomer/" + id;
        }

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

