package com.demoproject.controller;

import com.demoproject.billqueue.BillQueueProcessor;
import com.demoproject.billqueue.BillRequest;
import com.demoproject.dto.CustomerDataDTO;
import com.demoproject.dto.request.CustomerRequest;
import com.demoproject.entity.*;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.productqueue.ProductQueueProcessor;
import com.demoproject.repository.AccountRepository;
import com.demoproject.repository.BillRepository;
import com.demoproject.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/bill")
public class BillController {
    @Autowired
    private final UserService userService;
    private final AccountService accountService;
    private final ProductService productService;
    private final BillQueueProcessor billQueueProcessor;
    @Autowired
    private AccountRepository accountRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private CustomerService customerService;
    private BillRepository billRepository;
    private ProductQueueProcessor productQueueProcessor;
    private final ObjectMapper objectMapper;
    private final BillService billService;

    public BillController(AccountService accountService, UserService userService, ProductService productService, BillQueueProcessor billQueueProcessor, BillRepository billRepository, ProductQueueProcessor productQueueProcessor, ObjectMapper objectMapper,
                          BillService billService) {
        this.accountService = accountService;
        this.userService = userService;
        this.productService = productService;
        this.billQueueProcessor = billQueueProcessor;
        this.billRepository = billRepository;
        this.productQueueProcessor = productQueueProcessor;
        this.objectMapper = objectMapper;
        this.billService = billService;
    }

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/listBill")
    public String listBill(Model model, @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "2") int size, @RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "name") String searchBy,
                           @CookieValue(value = "token", required = false) String token,
                           RedirectAttributes redirectAttributes) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Bill> billPage;
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("STAFF")) {
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        if(search.isEmpty())
        {
            billPage = billService.getAllBillsByStoreId(pageable,user.getStoreId());
        }
        else
        {
            if(searchBy.equals("customerName")){
                billPage = billService.getBillsByStoreIdAndCustomerName(search,user.getStoreId(),pageable);
            }
            else{

                try{
                    Long billId = Long.parseLong(search);
                    billPage = billService.getBillsByStoreIdAndBillId(billId, user.getStoreId(), pageable);
                }
                catch(NumberFormatException e){
                    model.addAttribute("error", "Invalid Invoice ID. Please enter a valid number.");
                    billPage = billService.getAllBillsByStoreId(pageable, user.getStoreId());
                }
            }
        }
        model.addAttribute("bills", billPage);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", billPage.getTotalPages());
        return "bill/listBill";
    }

    @GetMapping("/billDetail")
    public String billDetail( Model model) {

        return "bill/billDetail";
    }

    @GetMapping("/createBill")
    public String createBill(@CookieValue(value = "token", required = false) String token,
                             Model model,
                             @RequestParam(required = false) String productName) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
         model.addAttribute("account", optAccount.get());
        model.addAttribute("user", user);
        String role= jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if(role.equals("STAFF")){
            listHiddenPage.add("listStaff");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);
        return "bill/createBill";
    }

    // Thêm API tìm kiếm sản phẩm
    @GetMapping("/searchProducts")
    @ResponseBody
    public List<Product> searchProducts(@RequestParam String keyword,@CookieValue(value = "token", required = false) String token) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        return productService.getProductsByNameAndCreatedBy(keyword,user.getStoreId());
    }

    @GetMapping("/searchCustomers")
    @ResponseBody
    public List<Customer> searchCustomers(@RequestParam String keyword, @CookieValue(value = "token", required = false) String token) {

        Account account = accountService.getAccountFromToken(token).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        List<Customer> customers= customerService.searchCustomer(user.getStoreId(),keyword);

        return customers;
    }


    @PostMapping("/addCustomer")
    @ResponseBody
    public Map<String, Object> addCustomer(@RequestBody CustomerRequest customerRequest,
                                           @CookieValue(value = "token", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Users user = userService.getUserProfile(account.getUserId()).orElse(null);
        String phone = customerRequest.getPhone().trim();
        customerRequest.setPhone(phone.trim());
        customerRequest.setMoneyState(0);
        customerRequest.setCreatedBy(user.getId());
        customerRequest.setCreatedAt(LocalDate.now());
        customerRequest.setStoreId(user.getStoreId());
        try {
            customerService.createCustomer(customerRequest);
            response.put("success", true);
            response.put("message", "Thêm khách hàng thành công!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi thêm khách hàng: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/checkout")
    @ResponseBody
    public Map<String, Object> sendBillToQueue(@RequestBody Bill bill, @CookieValue(value = "token", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = jwtUtils.extractUsername(token);
            Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
            Users user = userService.getUserProfile(optAccount.get().getUserId()).orElse(null);
            ObjectMapper objectMapper = new ObjectMapper();
            CustomerDataDTO customerData = objectMapper.readValue(bill.getCustomerData(), CustomerDataDTO.class);

            String customerDataJson = objectMapper.writeValueAsString(customerData);

            // 🔹 Lưu hóa đơn ngay lập tức vào database
            bill.setCreatedBy(user.getId());
            bill.setStoreId(user.getStoreId());
            bill.setStatus(true); // ✅ Đánh dấu hóa đơn đã xử lý xong
            bill = billRepository.save(bill); // ✅ Lưu và lấy `billId`


            List<Product> products = objectMapper.readValue(bill.getProductData(), objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class));

            // ✅ Đưa danh sách sản phẩm vào hàng đợi để giảm số lượng
            productQueueProcessor.addProductsToQueue(products);

            response.put("success", true);
            response.put("message", "Hóa đơn đã được đưa vào hàng đợi để xử lý!");
            response.put("billId", bill.getId());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi gửi hóa đơn: " + e.getMessage());
        }
        return response;
    }



}
