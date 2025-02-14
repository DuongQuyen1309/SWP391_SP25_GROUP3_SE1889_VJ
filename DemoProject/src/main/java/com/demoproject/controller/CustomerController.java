package com.demoproject.controller;

import com.demoproject.dto.reponse.CustomerResponse;
import com.demoproject.dto.request.CustomerRequest;
import com.demoproject.entity.Account;
import com.demoproject.entity.Customer;
import com.demoproject.mapper.CustomerMapper;
import com.demoproject.repository.CustomerRepository;
import com.demoproject.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerMapper customerMapper;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/listCustomer")
    public String listCustomer(Model model, @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Customer> customerPage = customerService.getAllCustomersAndIsDeleteFalse(pageable);

        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());

        return "listCustomer";
    }

    @GetMapping("/createCustomer")
    public String createCustomer() {
        return "createCustomer";
    }

    @PostMapping("/createCustomer")
    public String createCustomerList(@RequestParam String firstname,
                                     @RequestParam String lastname,
                                     @RequestParam String address,
                                     @RequestParam LocalDate dob,
                                     @RequestParam String phone,
                                    @RequestParam Boolean gender,
                                     Model model,  RedirectAttributes redirectAttributes) {
        CustomerRequest customer = new CustomerRequest();
        String name = firstname + " " + lastname;
        customer.setName(name);
        customer.setAddress(address);
        customer.setDob(dob);
        customer.setPhone(phone);
        customer.setGender(gender);
        customer.setMoneyState(0);
        // Gọi service để tạo khách hàng (giả định khách hàng được tạo thành công)
        try {
            customerService.createCustomer(customer);
            // Thêm thông báo thành công vào model
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Customer created successfully!");
        } catch (Exception e) {
            // Thêm thông báo thất bại nếu có lỗi
            redirectAttributes.addFlashAttribute("messageType", "fail");
            redirectAttributes.addFlashAttribute("message", "Failed to create customer.");
        }

        // Sau khi tạo khách hàng thành công, chuyển hướng về danh sách khách hàng
        return "redirect:/listCustomer";
    }

    @GetMapping("/updateCustomer/{id}")
    public String getUpdatedCustomer(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomer(id);
        System.out.println("ngay sinh la "+ customer.getId());
        model.addAttribute("customer", customer);  // Truyền customer vào model
        return "updateCustomer";
    }

    @PostMapping("/updateCustomer")
    public String updateCustomer(@RequestParam String id,
                                 @RequestParam String name,
                                 @RequestParam String address,
                                 @RequestParam String dob,
                                 @RequestParam String phone,
                                 @RequestParam String gender) {
        Customer customer = new Customer();
        customer.setId(Long.parseLong(id));
        customer.setName(name);
        customer.setAddress(address);
        customer.setPhone(phone);
        customer.setDob(LocalDate.parse(dob));
        customer.setGender(Boolean.parseBoolean(gender));
        customerService.updateCustomer(Long.parseLong(id), customer); // Call service to update customer
        return "redirect:/listCustomer"; // Redirect to listCustomer after update
    }

}
