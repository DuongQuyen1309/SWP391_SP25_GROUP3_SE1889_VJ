package com.demoproject.controller;

import com.demoproject.dto.DashBoardDTO;
import com.demoproject.dto.TopProductDTO;
import com.demoproject.entity.Account;
import com.demoproject.entity.Bill;
import com.demoproject.entity.Store;
import com.demoproject.entity.Users;
import com.demoproject.jwt.JwtUtils;
import com.demoproject.repository.DashBoardRepository;
import com.demoproject.service.AccountService;
import com.demoproject.service.DashBoardService;
import com.demoproject.service.StoreService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/dashboard")
@Controller
public class DashBoardController {
    @Autowired
    private final StoreService storeService;
    @Autowired
    private DashBoardService dashboardService;
    @Autowired
    private DashBoardRepository dashboardRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;


    public DashBoardController(StoreService storeService, DashBoardService dashboardService, DashBoardRepository dashboardRepository, AccountService accountService, UserService userService, JwtUtils jwtUtils) {
        this.storeService = storeService;
        this.dashboardService = dashboardService;
        this.dashboardRepository = dashboardRepository;
        this.accountService = accountService;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/api/dashboard")
    @ResponseBody
    public DashBoardDTO getDashboardData(@CookieValue(value = "token", required = false) String token) {
        String username = jwtUtils.extractUsername(token);
        Store store = storeService.findStoreByAccount(username);

        if (store == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cửa hàng không tồn tại");
        }

        return dashboardService.getDashboardData(store.getId());
    }

    @GetMapping("view")
    public String viewDashboard(Model model,
                                @CookieValue(value = "token", required = false) String token) {
        // Lấy username từ token
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);

        if (optAccount.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy tài khoản");
        }

        Account account = optAccount.get();
        Optional<Users> userOpt = userService.getUserProfile(account.getUserId());
        Users user = userOpt.orElse(new Users());

        // Lấy Store của Owner
        Store store = storeService.findStoreByAccount(username);
        if (store == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cửa hàng không tồn tại");
        }

        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        // Gọi service với storeId
        DashBoardDTO dashboardData = dashboardService.getDashboardData(store.getId());

        model.addAttribute("user", user);
        model.addAttribute("account", account);
        model.addAttribute("dashboardData", dashboardData);

        System.out.println("📌 Token nhận được: " + token);
        System.out.println("📌 Store ID: " + store.getId());

        return "dashboard/thongke"; // Trả về file dashboard/thongke.html
    }


    @GetMapping("")
    public String dashboard(@RequestParam(defaultValue = "monthly") String period,
                            @RequestParam(required = false) Integer month,
                            @RequestParam(required = false) Integer year,
                            @CookieValue(value = "token", required = false) String token,
                            Model model) {
        LocalDateTime now = LocalDateTime.now();

        // Nếu không có month/year thì dùng tháng hiện tại
        if (month == null) {
            month = now.getMonthValue();
        }
        if (year == null) {
            year = now.getYear();
        }

        LocalDateTime startDate;
        LocalDateTime endDate;

        // Thêm xử lý cho monthly
        if (period.equals("daily")) {
            // Nếu daily → lấy từ ngày đầu tháng đến cuối tháng người chọn
            startDate = LocalDateTime.of(year, month, 1, 0, 0);
            endDate = startDate.withDayOfMonth(startDate.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59);
        } else if (period.equals("weekly")) {
            startDate = now.minusWeeks(12);
            endDate = now;
        } else if (period.equals("monthly")) {
            // Nếu monthly → lấy từ tháng 1 năm nay đến tháng hiện tại của năm đó
            startDate = LocalDateTime.of(year, 1, 1, 0, 0);
            endDate = now;
        } else {
            startDate = now.minusYears(5);
            endDate = now;
        }

        List<Object[]> rawData;
        if (period.equals("daily")) {
            rawData = dashboardRepository.getRevenueByDay(startDate, endDate);
        } else if (period.equals("weekly")) {
            rawData = dashboardRepository.getRevenueByWeek(startDate, endDate);
        } else if (period.equals("monthly")) {
            rawData = dashboardRepository.getRevenueByMonth(startDate, endDate);
        } else {
            rawData = dashboardRepository.getRevenueByYear(startDate, endDate);
        }
        String role = jwtUtils.extractRole(token);
        List<String> listHiddenPage = new ArrayList<>();
        listHiddenPage.add("");
        if (role.equals("OWNER")) {
            listHiddenPage.add("listOwner");
        }
        model.addAttribute("listHiddenPage", listHiddenPage);

        List<String> labels = dashboardService.getRevenueLabels(rawData, period, month, year);
        List<Integer> values = dashboardService.getRevenueValues(rawData, period, month, year);
        Account account = accountService.getAccountFromToken(token).orElse(null);
        Optional<Users> user = userService.getUserProfile(account.getUserId());

        model.addAttribute("labels", labels);
        model.addAttribute("values", values);
        model.addAttribute("period", period);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        System.out.println("📌 Token nhận được: " + token);

        return "dashboard/thongke";
    }




    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(@RequestParam int month,
                                                              @RequestParam int year,
                                                              @CookieValue(value = "token", required = false) String token) {
        // Lấy username từ token
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        if (optAccount.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        // Lấy storeId của Owner
        Store store = storeService.findStoreByAccount(username);
        Long storeId = store.getId();

        // Lấy danh sách sản phẩm top bán chạy của cửa hàng theo tháng
        List<TopProductDTO> topProducts = dashboardService.getTopProductsByQuantity(month, year, storeId);

        System.out.println("Month: " + month + ", Year: " + year + ", StoreID: " + storeId + ", Products: " + topProducts.size());
        return ResponseEntity.ok(topProducts);
    }


    @GetMapping("/revenue/daily")
    public ResponseEntity<Map<String, Object>> getDailyRevenue(@RequestParam int month, @RequestParam int year,
                                                               @CookieValue(value = "token", required = false) String token) {

        // Lấy username từ token
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        if (optAccount.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Không tìm thấy tài khoản"));
        }

        Account account = optAccount.get();
        Store store = storeService.findStoreByAccount(username);
        Long storeId = store.getId(); // Lấy storeId của Owner

        List<Object[]> results = dashboardService.getRevenueByDayInMonth(storeId,month, year);

        // Tạo map chứa doanh thu cho từng ngày
        Map<Integer, Integer> revenueMap = new HashMap<>();
        for (Object[] row : results) {
            int day = ((Number) row[0]).intValue();
            int revenue = ((Number) row[1]).intValue();
            revenueMap.put(day, revenue);
        }

        // Lấy số ngày trong tháng
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Tạo danh sách đầy đủ các ngày trong tháng với doanh thu mặc định là 0
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            Map<String, Object> map = new HashMap<>();
            map.put("day", day);
            map.put("revenue", revenueMap.getOrDefault(day, 0));
            responseList.add(map);
        }

        // Trả về dữ liệu cùng với tiêu đề tháng
        Map<String, Object> response = new HashMap<>();
        response.put("data", responseList);
        response.put("monthTitle", "Tháng " + month + "/" + year);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/revenue/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyRevenue(@RequestParam int year,
                                                                 @CookieValue(value = "token", required = false) String token) {
        // Lấy username từ token
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        if (optAccount.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Không tìm thấy tài khoản"));
        }

        Account account = optAccount.get();
        Store store = storeService.findStoreByAccount(username);
        Long storeId = store.getId(); // Lấy storeId của Owner
        List<Object[]> results = dashboardService.getRevenueByMonthInYear(year,storeId);

        Map<Integer, Integer> revenueMap = new HashMap<>();
        for (Object[] row : results) {
            int month = ((Number) row[0]).intValue();
            int revenue = ((Number) row[1]).intValue();
            revenueMap.put(month, revenue);
        }

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            Map<String, Object> map = new HashMap<>();
            map.put("month", month);
            map.put("revenue", revenueMap.getOrDefault(month, 0)); // Mặc định 0 nếu không có doanh thu
            responseList.add(map);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", responseList);
        response.put("yearTitle", "Năm " + year);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/revenue/yearly")
    public ResponseEntity<List<Map<String, Object>>> getYearlyRevenue() {
        int currentYear = LocalDateTime.now().getYear();
        int startYear = currentYear - 4; // Lấy 5 năm gần nhất

        List<Object[]> results = dashboardService.getRevenueByYearRange(startYear, currentYear);

        // Tạo map mặc định với tất cả năm có doanh thu = 0
        Map<Integer, Integer> revenueMap = new HashMap<>();
        for (int year = startYear; year <= currentYear; year++) {
            revenueMap.put(year, 0);
        }

        // Cập nhật doanh thu từ kết quả truy vấn
        for (Object[] row : results) {
            int year = ((Number) row[0]).intValue();
            int revenue = ((Number) row[1]).intValue();
            revenueMap.put(year, revenue);
        }

        // Chuyển dữ liệu về List<Map<String, Object>> để trả về JSON
        List<Map<String, Object>> response = new ArrayList<>();
        for (int year = startYear; year <= currentYear; year++) {
            Map<String, Object> map = new HashMap<>();
            map.put("year", year);
            map.put("revenue", revenueMap.get(year));
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }


    @GetMapping("/bills/today")
    public ResponseEntity<Page<Map<String, Object>>> getBillsToday(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        // Lấy username từ token
        String username = jwtUtils.extractUsername(token);
        Optional<Account> optAccount = accountService.findByUsernameAndIsDeleteFalse(username);
        if (optAccount.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Page.empty());
        }

        // Lấy storeId của Owner
        Store store = storeService.findStoreByAccount(username);
        Long storeId = store.getId();

        // Lấy danh sách hóa đơn hôm nay có phân trang
        Page<Bill> bills = dashboardService.getBillsToday(storeId, PageRequest.of(page, size, Sort.by("createdAt").descending()));

        // Chuyển đổi dữ liệu thành danh sách JSON đúng format
        Page<Map<String, Object>> response = bills.map(bill -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", bill.getId());
            map.put("customerName", bill.getCustomerName() != null ? bill.getCustomerName() : "Khách lẻ");
            map.put("totalMoney", bill.getTotalMoney());
            map.put("createdAt", bill.getCreatedAt());
            return map;
        });

        return ResponseEntity.ok(response);
    }






}
