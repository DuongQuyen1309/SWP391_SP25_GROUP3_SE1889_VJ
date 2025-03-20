package com.demoproject.controller;

import com.demoproject.billqueue.BillQueueProcessor;
import com.demoproject.billqueue.BillRequest;
import com.demoproject.dto.CustomerDataDTO;
import com.demoproject.dto.ProductDataDTO;
import com.demoproject.dto.request.CustomerRequest;
import com.demoproject.entity.*;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.productqueue.ProductQueueProcessor;
import com.demoproject.repository.AccountRepository;
import com.demoproject.repository.BillRepository;
import com.demoproject.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
                           @RequestParam(defaultValue = "5") int size,
                           @RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "name") String searchBy,
                           @RequestParam(defaultValue = "id") String sortField,
                           @RequestParam(defaultValue = "asc") String sortOrder,
                           @RequestParam(required = false) Double minValue,
                           @RequestParam(required = false) Double maxValue,
                           @CookieValue(value = "token", required = false) String token,
                           RedirectAttributes redirectAttributes) {

        Sort sort;
        if (sortField.equals("debtMoney")) {
            sort = sortOrder.equalsIgnoreCase("desc") ?
                    Sort.by("debtMoney").descending() :
                    Sort.by("debtMoney").ascending();
        } else {
            // Default sort by id
            sort = sortOrder.equalsIgnoreCase("desc") ?
                    Sort.by("id").descending() :
                    Sort.by("id").ascending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
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

        // Add range values to model for form persistence
        model.addAttribute("minValue", minValue);
        model.addAttribute("maxValue", maxValue);

        // Handle total money range search
        if (searchBy.equals("totalMoneyRange") && minValue != null && maxValue != null) {
            billPage = billService.getBillsByTotalMoneyRange(minValue, maxValue, user.getStoreId(), pageable);
        } else if (search.isEmpty()) {
            billPage = billService.getAllBillsByStoreId(pageable, user.getStoreId());
        } else {
            if (searchBy.equals("customerName")) {
                billPage = billService.getBillsByStoreIdAndCustomerName(search, user.getStoreId(), pageable);
            } else {
                try {
                    Long billId = Long.parseLong(search);
                    billPage = billService.getBillsByStoreIdAndBillId(billId, user.getStoreId(), pageable);
                } catch (NumberFormatException e) {
                    model.addAttribute("error", "Invalid Invoice ID. Please enter a valid number.");
                    billPage = billService.getAllBillsByStoreId(pageable, user.getStoreId());
                }
            }
        }

        model.addAttribute("bills", billPage);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", billPage.getTotalPages());
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("search", search);
        model.addAttribute("searchBy", searchBy);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortOrder", sortOrder);

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
        List<Product> products = productService.getProductsByNameAndStoreIdAndQuantityGreaterThanZero(keyword, user.getStoreId());
        for(Product product : products){
            System.out.println(product.getPackageType());
        }
        return products;
    }

    @GetMapping("/searchImportProducts")
    @ResponseBody
    public List<Product> searchImportProducts(@RequestParam String keyword,@CookieValue(value = "token", required = false) String token) {
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountRepository.findByUsernameAndIsDeleteFalse(username);
        Optional<Users> userOpt= userService.getUserProfile(optAccount.get().getUserId());
        Users user = userOpt.orElse(new Users());
        List<Product> products = productService.getProductsByNameAndStoreId(keyword, user.getStoreId());
        for(Product product : products){
            System.out.println(product.getPackageType());
        }
        return products;
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
            List<ProductDataDTO> products = objectMapper.readValue(bill.getProductData(), new TypeReference<List<ProductDataDTO>>() {});
            for (ProductDataDTO product : products) {
                System.out.println("📌 Backend nhận Product ID: " + product.getId() +
                        ", selectedPackage: " + product.getSelectedPackage());
            }
            System.out.println("String : "+ bill.getProductData());

// ✅ Kiểm tra sản phẩm trong kho
            // ✅ Tạo một Map để nhóm các sản phẩm theo ID và tính tổng số kg
            Map<Long, Integer> totalKgPerProduct = new HashMap<>();

            for (ProductDataDTO product : products) {
                int packageSize = extractPackageSize(product.getPackageTypeName()); // Lấy số kg từ tên gói
                int totalKg = product.getQuantity() * packageSize;
                System.out.println(product);
                System.out.println("totalKG: "+totalKg);
                System.out.println("packageSize: "+packageSize);
                System.out.println("product.getQuantity(): "+product.getQuantity());
                System.out.println("sss: "+ extractPackageSize(product.getPackageTypeName()));
                System.out.println("product.getPackageTypeName(): "+product.getPackageTypeName());

                totalKgPerProduct.merge(product.getId(), totalKg, Integer::sum);
            }
            // ✅ Kiểm tra số kg với database
            for (Map.Entry<Long, Integer> entry : totalKgPerProduct.entrySet()) {
                Long productId = entry.getKey();
                int totalKgRequired = entry.getValue();

                Product product = productService.getProductById(productId);
                if (product == null || product.getQuantity() < totalKgRequired) {
                    System.out.println("❌ Không đủ hàng trong kho cho sản phẩm ID " + productId);
                    response.put("success", false);
                    response.put("message", "❌ Sản phẩm '" + product.getName() + "' không đủ số lượng trong kho!");
                    return response;
                }
            }
//            Map<Long, Integer> totalQuantityByProductId = new HashMap<>();
//            for(ProductDataDTO productDataDTO : products){
//                System.out.println("Id: " + productDataDTO.getId());
//                System.out.println("Quantity: " + productDataDTO.getQuantity());
//                totalQuantityByProductId.put(productDataDTO.getId(), totalQuantityByProductId.getOrDefault(productDataDTO.getId(), 0) + productDataDTO.getQuantity());
//            }
//
//            for(Map.Entry<Long, Integer> entry : totalQuantityByProductId.entrySet()){
//                Long productId = entry.getKey();
//                System.out.println("Id: " + productId);
//                Integer quantity = entry.getValue();
//                System.out.println("Quantity: " + quantity);
//                Product product = productService.getProductById(productId);
//                if(product == null || product.getQuantity() < quantity){
//                    response.put("success", false);
//                    response.put("message", "❌ Sản phẩm '" + product.getName() + "' không đủ số lượng trong kho!");
//                    return response;
//                }
//            }
            String updatedProductData = objectMapper.writeValueAsString(products);
            bill.setProductData(updatedProductData);
            String trackingId = UUID.randomUUID().toString();
            System.out.println(trackingId);
            bill.setCreatedBy(user.getId());
            bill.setCreatedAt(LocalDateTime.now());
            System.out.println(bill.getCreatedAt());
            bill.setStoreId(user.getStoreId());

            BillRequest billRequest = new BillRequest(
                    bill.getTotalMoney(),
                    bill.getPaidMoney(),
                    bill.getDebtMoney(),
                    bill.getProductData(),
                    bill.getCustomerData(),
                    user.getId(),
                    bill.isPorted(),
                    bill.isDebt(),
                    user.getStoreId(),
                    false,
                    bill.getNote(),
                    bill.getDiscount(),
                    bill.getPortedMoney(),
                    trackingId
            );

            System.out.println("Trước add");
            // ✅ Thêm hóa đơn vào hàng đợi để xử lý sau
            billQueueProcessor.addBill(trackingId, billRequest);

            System.out.println("Sau add");
            System.out.println(trackingId);
            response.put("message", "Hóa đơn đã được đưa vào hàng đợi để xử lý!");
            response.put("success", true);
            response.put("trackingId", trackingId);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi gửi hóa đơn: " + e.getMessage());
        }
        return response;
    }

    private int extractPackageSize(String packageTypeName) {
        try {
            return Integer.parseInt(packageTypeName.replaceAll("[^0-9]", "")); // Lấy số từ "1kg", "5kg"
        } catch (Exception e) {
            return 1; // Mặc định nếu không tìm thấy số
        }
    }

    @GetMapping("/bill/billDetail")
    public String billDetail(@RequestParam Long id, @CookieValue(value = "token", required = false) String token, Model model) {
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
        Optional<Bill> billOpt = billService.getBillByIdAndStoreId(id, user.getStoreId());
        if (billOpt.isPresent()) {
            Bill bill = billOpt.get();
            model.addAttribute("bill", bill);
            model.addAttribute("customer", bill.getCustomer());
            System.out.println(bill.getProducts());
            List<ProductDataDTO> products = bill.getProducts();
            for (ProductDataDTO product : products) {
                System.out.println("🔍 Product ID: " + product.getId());
                System.out.println("🔍 packageType: " + product.getPackageType());
            }
            List<ProductDataDTO> productDTOs = bill.getProducts().stream().map(product -> {
                ProductDataDTO dto = new ProductDataDTO();
                dto.setName(product.getName());
                dto.setQuantity(product.getQuantity());
                dto.setPrice(product.getPrice());

                dto.setTotal(dto.getQuantity() * dto.getPrice()); // ✅ Tính tổng theo số kg
                dto.setPackageTypeName(product.getPackageTypeName()); // ✅ Thêm thông tin đóng gói
                dto.setSelectedPackageSize(product.getSelectedPackageSize());



                return dto;
            }).collect(Collectors.toList());

            model.addAttribute("products", productDTOs);
        }else {
            model.addAttribute("error", "Bill not found");
        }
        return "bill/billDetail";
    }

    @GetMapping("/tracking/{trackingId}")
    public ResponseEntity<Map<String, Object>> checkBillStatus(@PathVariable String trackingId) {
        Long billId = billQueueProcessor.getBillIdByTrackingId(trackingId);
        System.out.println(billId);

        Map<String, Object> response = new HashMap<>();

        if (billId != null) {
            response.put("status", "processed");
            response.put("billId", billId);
        } else {
            response.put("status", "pending");
        }

        return ResponseEntity.ok(response);
    }



}
