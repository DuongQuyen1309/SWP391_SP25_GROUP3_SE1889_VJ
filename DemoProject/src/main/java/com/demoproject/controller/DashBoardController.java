package com.demoproject.controller;

import com.demoproject.dto.DashboardDTO;
import com.demoproject.dto.TopProductDTO;
import com.demoproject.repository.DashboardRepository;
import com.demoproject.service.DashboardService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/dashboard")
@Controller
public class DashBoardController {
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private DashboardRepository dashboardRepository;
    @GetMapping("view")
    public String viewDashboard(Model model) {
        DashboardDTO dashboardData = dashboardService.getDashboardData();
        model.addAttribute("dashboardData", dashboardData);
        return "dashboard/thongke"; // Sẽ trả về file templates/dashboard.html
    }

    @GetMapping("/api/dashboard")
    @ResponseBody
    public DashboardDTO getDashboardData() {
        return dashboardService.getDashboardData();
    }

    @GetMapping("")
    public String dashboard(@RequestParam(defaultValue = "monthly") String period,
                            @RequestParam(required = false) Integer month,
                            @RequestParam(required = false) Integer year,
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

        List<String> labels = dashboardService.getRevenueLabels(rawData, period, month, year);
        List<Integer> values = dashboardService.getRevenueValues(rawData, period, month, year);

        model.addAttribute("labels", labels);
        model.addAttribute("values", values);
        model.addAttribute("period", period);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);

        return "dashboard/thongke";
    }



    @GetMapping("/api/revenue")
    @ResponseBody
    public Map<String, Object> getRevenueData(@RequestParam(defaultValue = "monthly") String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;

        if (period.equals("daily")) {
            startDate = now.minusDays(30);
        } else if (period.equals("weekly")) {
            startDate = now.minusWeeks(12);
        } else if (period.equals("monthly")) {
            startDate = now.minusMonths(12);
        } else {
            startDate = now.minusYears(5);
        }

        List<Object[]> rawData;
        if (period.equals("daily")) {
            rawData = dashboardRepository.getRevenueDailyThisMonth();
        } else if (period.equals("weekly")) {
            rawData = dashboardRepository.getRevenueByWeek(startDate, now);
        } else if (period.equals("monthly")) {
            rawData = dashboardRepository.getRevenueByMonth(startDate, now);
        } else {
            rawData = dashboardRepository.getRevenueByYear(startDate, now);
        }

        int month = now.getMonthValue();
        int year = now.getYear();

        List<String> labels = dashboardService.getRevenueLabels(rawData, period, month, year);
        List<Integer> values = dashboardService.getRevenueValues(rawData, period, month, year);

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("revenues", values);

        if (period.equals("daily")) {
            response.put("monthTitle", "Tháng " + month + "/" + year);
        }

        return response;
    }


    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(@RequestParam int month, @RequestParam int year) {
        List<TopProductDTO> topProducts = dashboardService.getTopProductsByQuantity(month, year);
        System.out.println("Month: " + month + ", Year: " + year + ", Products: " + topProducts.size());
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyRevenue(
            @RequestParam int month, @RequestParam int year) {
        List<Object[]> results = dashboardService.getRevenueByDayInMonth(month, year);
        List<Map<String, Object>> response = results.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("day", r[0]);
            map.put("revenue", r[1]);
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyRevenue(@RequestParam int year) {
        List<Object[]> results = dashboardService.getRevenueByMonthInYear(year);
        List<Map<String, Object>> response = results.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("month", r[0]);
            map.put("revenue", r[1]);
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

}
