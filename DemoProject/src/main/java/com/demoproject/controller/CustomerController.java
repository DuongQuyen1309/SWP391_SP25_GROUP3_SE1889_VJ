package com.demoproject.controller;

import com.demoproject.dto.reponse.CustomerResponse;
import com.demoproject.dto.request.CustomerRequest;
import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.mapper.CustomerMapper;
import com.demoproject.repository.CustomerRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.CustomerService;
import com.demoproject.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String ctype) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Customer> customerPage;

        if ((search != null && !search.isEmpty()) || (ctype != null && !ctype.isEmpty())) {
            customerPage = customerService.searchCustomersByNameAndType(search, ctype, pageable);
        } else {
            customerPage = customerService.getAllCustomersAndIsDeleteFalse(pageable);
        }

        List<String> customerTypes = customerService.getAllCustomerTypes();

        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("ctype", ctype);  // Đảm bảo giá trị ctype được truyền vào Thymeleaf
        model.addAttribute("customerTypes", customerTypes);

        return "listCustomer";
    }


    //    @GetMapping("/createCustomer")
//    public String createCustomer() {
//        return "createCustomer";
//    }
//
//    @PostMapping("/createCustomer")
//    public String createCustomerList(@RequestParam String firstname,
//                                     @RequestParam String lastname,
//                                     @RequestParam String address,
//                                     @RequestParam LocalDate dob,
//                                     @RequestParam String phone,
//                                     @RequestParam Boolean gender,
//                                     @RequestParam String ctype,
//                                     @CookieValue(value = "token", required = false) String token,
//                                     Model model,  RedirectAttributes redirectAttributes) {
//        String username = jwtUtils.extractUsername(token);
//        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//        Account account= accountService.findByUsername(username);
//        Users user= userService.getUserProfile(account.getUserId());
//        Long id = user.getId();
//
//        CustomerRequest customer = new CustomerRequest();
//        String name = firstname + " " + lastname;
//        customer.setName(name);
//        customer.setAddress(address);
//        customer.setDob(dob);
//        customer.setPhone(phone);
//        customer.setGender(gender);
//        customer.setCreatedBy(id);
//        System.out.println("nguoi tao la: "+ customer.getCreatedBy());
//        customer.setCreatedAt(LocalDate.now());
//        System.out.println("ngay tao la: "+ customer.getCreatedAt());
//        customer.setMoneyState(0);
//        customer.setCtype(ctype);
//        // Gọi service để tạo khách hàng (giả định khách hàng được tạo thành công)
//        try {
//            customerService.createCustomer(customer);
//            System.out.print("controller "+ customer.getCtype());
//            // Thêm thông báo thành công vào model
//            redirectAttributes.addFlashAttribute("messageType", "success");
//            redirectAttributes.addFlashAttribute("message", "Customer created successfully!");
//        } catch (Exception e) {
//            // Thêm thông báo thất bại nếu có lỗi
//            redirectAttributes.addFlashAttribute("messageType", "fail");
//            redirectAttributes.addFlashAttribute("message", "Failed to create customer.");
//        }
//
//        // Sau khi tạo khách hàng thành công, chuyển hướng về danh sách khách hàng
//        return "redirect:/listCustomer";
//    }
    @GetMapping("/createCustomer")
    public String createCustomer(Model model) {
        if (!model.containsAttribute("customerRequest")) {
            model.addAttribute("customerRequest", new CustomerRequest());
        }
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

        int check = 0;
        for(int i=0;i<lastname.length();i++){
            if(Character.isDigit(lastname.charAt(i)) || !Character.isLetterOrDigit(lastname.charAt(i))){
                check++;
            }
        }

        if (result.hasErrors() || !lastname.matches("^[a-zA-ZÀ-Ỹà-ỹ\\s]+$") || lastname.trim().isEmpty()) {
            model.addAttribute("customerRequest", customerRequest);
            model.addAttribute("lastnamemessage","Not be empty, not contain number and special characters");
            return "createCustomer"; // Quay lại form nếu có lỗi
        }

        String username = jwtUtils.extractUsername(token);
        Account account = accountService.findByUsername(username);
        Users user = userService.getUserProfile(account.getUserId());
        Long id = user.getId();

        String fullname = customerRequest.getName().trim() + " " + lastname.trim();
        customerRequest.setName(fullname);
        String phone = customerRequest.getPhone().trim();
        customerRequest.setPhone(phone);
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

        return "redirect:/listCustomer";
    }


    @GetMapping("/updateCustomer/{id}")
    public String getUpdatedCustomer(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomer(id);
        System.out.println("ngay sinh la " + customer.getId());
        model.addAttribute("customer", customer);  // Truyền customer vào model
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
        RedirectAttributes redirectAttributes) {



    if (name.trim().isEmpty()) {
        redirectAttributes.addFlashAttribute("messageType", "fail");
        redirectAttributes.addFlashAttribute("message", "Name cannot be empty.");
        return "redirect:/updateCustomer/" + id;
    }

    // Kiểm tra Address
    if (address.trim().isEmpty()) {
        redirectAttributes.addFlashAttribute("messageType", "fail");
        redirectAttributes.addFlashAttribute("message", "Address cannot be empty.");
        return "redirect:/updateCustomer/" + id;
    }

    // Kiểm tra Date of Birth
    LocalDate dateOfBirth;
    try {
        dateOfBirth = LocalDate.parse(dob);
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("messageType", "fail");
        redirectAttributes.addFlashAttribute("message", "Invalid date format. Please use yyyy-MM-dd.");
        return "redirect:/updateCustomer/" + id;
    }

    if (dateOfBirth.isAfter(LocalDate.now())) {
        redirectAttributes.addFlashAttribute("messageType", "fail");
        redirectAttributes.addFlashAttribute("message", "Date of Birth cannot be a future date.");
        return "redirect:/updateCustomer/" + id;
    }

    // Kiểm tra Phone
    if (!phone.matches("^[0-9]{10,11}$")) {
        redirectAttributes.addFlashAttribute("messageType", "fail");
        redirectAttributes.addFlashAttribute("message", "Phone number must be between 10 and 11 digits and contain only numbers.");
        return "redirect:/updateCustomer/" + id;
    }

    // Tạo đối tượng Customer để cập nhật
    Customer customer = new Customer();
    customer.setId(Long.parseLong(id));
    customer.setName(name);
    customer.setAddress(address);
    customer.setPhone(phone.trim());
    customer.setGender(gender);
    customer.setDob(dateOfBirth);
    customer.setCtype(ctype);
    customer.setUpdatedAt(LocalDate.now());

    // Lưu vào database
    customerService.updateCustomer(Long.parseLong(id), customer);

    redirectAttributes.addFlashAttribute("messageType", "success");
    redirectAttributes.addFlashAttribute("message", "Customer updated successfully!");

    return "redirect:/listCustomer";
}

}

