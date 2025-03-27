package com.demoproject.service;

import com.demoproject.dto.DashBoardDTO;
import com.demoproject.dto.ProductDataDTO;
import com.demoproject.dto.TopProductDTO;
import com.demoproject.entity.Bill;
import com.demoproject.repository.DashBoardRepository;
import com.demoproject.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashBoardService {
    @Autowired
    private DashBoardRepository dashboardRepository;
    @Autowired
    private ProductRepository productRepository;

    public DashBoardDTO getDashboardData(Long storeId) {
        DashBoardDTO dto = new DashBoardDTO();
        Long totalRevenue= dashboardRepository.getTotalRevenue(storeId);
        if(totalRevenue==null){
            totalRevenue=0L;
        }
        dto.setTotalRevenue(totalRevenue);

        // Doanh thu hôm nay
        Long todayRevenue=dashboardRepository.getTodayRevenue(storeId);
        if(todayRevenue==null){
            todayRevenue=0L;
        }
        dto.setTodayRevenue(todayRevenue);
        Long todayBillCount= dashboardRepository.getTodayBillCount(storeId);
        if(todayBillCount==null){
            todayBillCount=0L;
        }
        dto.setTodayBillCount(todayBillCount);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1); // Lấy ngày hôm qua

        dto.setYesterdayRevenue(dashboardRepository.getRevenueByDate(yesterday,storeId));
        dto.setYesterdayBillCount(dashboardRepository.getBillCountByDate(yesterday,storeId));

        Long todayRevenue1 = dashboardRepository.getRevenueByDate(today,storeId);
        if(todayRevenue1== null){
            todayRevenue1=0L;
        }
        Long yesterdayRevenue = dashboardRepository.getRevenueByDate(yesterday,storeId);
        if(yesterdayRevenue== null){
            yesterdayRevenue=0L;
        }
        // Tính % thay đổi doanh thu
        if (yesterdayRevenue != null && yesterdayRevenue > 0) {
            double revenueChange = ((double) (todayRevenue1 - yesterdayRevenue) / yesterdayRevenue) * 100;
            dto.setRevenueChangePercentage(revenueChange);
        } else {
            dto.setRevenueChangePercentage(0.0);
        }
        System.out.println("storeId "+storeId);

        dto.setLowStockCount(productRepository.countLowStockProducts(storeId));



        return dto;
    }


    public List<String> getRevenueLabels(List<Object[]> rawData, String period, int month, int year) {
        List<String> labels = new ArrayList<>();

        if (period.equals("daily")) {
            // Lấy số ngày trong tháng hiện tại
            LocalDate selectedDate = LocalDate.of(year, month, 1);
            int daysInMonth = selectedDate.lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                labels.add("" + day);
            }
        } else if (period.equals("weekly")) {
            // Lấy số tuần duy nhất từ rawData, sắp xếp tăng dần
            Set<Integer> weekSet = new TreeSet<>();
            for (Object[] row : rawData) {
                int week = ((Number) row[1]).intValue();  // row[1] là tuần
                weekSet.add(week);
            }

            for (Integer week : weekSet) {
                labels.add("Tuần " + week);
            }
        } else if (period.equals("monthly")) {
            // Lấy tên các tháng trong năm
            for (int i = 1; i <= 12; i++) {
                labels.add("Tháng " + i);
            }
        } else if (period.equals("yearly")) {
            // Lấy các năm từ rawData
            for (Object[] row : rawData) {
                labels.add("Năm " + row[0].toString());
            }
        }

        return labels;
    }



    public List<Integer> getRevenueValues(List<Object[]> rawData, String period, int month, int year) {
        List<Integer> values = new ArrayList<>();

        if (period.equals("daily")) {
            // Doanh thu theo ngày trong tháng
            Map<Integer, Integer> dayRevenueMap = new HashMap<>();
            for (Object[] row : rawData) {
                int day = ((Number) row[0]).intValue();
                int revenue = ((Number) row[1]).intValue();
                dayRevenueMap.put(day, revenue);
            }

            LocalDate selectedDate = LocalDate.of(year, month, 1);
            int daysInMonth = selectedDate.lengthOfMonth();

            for (int day = 1; day <= daysInMonth; day++) {
                values.add(dayRevenueMap.getOrDefault(day, 0));
            }
        } else if (period.equals("weekly")) {
            // Doanh thu theo tuần
            Map<Integer, Integer> weekRevenueMap = new HashMap<>();
            Set<Integer> weekSet = new TreeSet<>();

            for (Object[] row : rawData) {
                int week = ((Number) row[1]).intValue();  // row[1] là tuần
                int revenue = ((Number) row[2]).intValue(); // row[2] là doanh thu
                weekRevenueMap.put(week, revenue);
                weekSet.add(week);
            }

            for (Integer week : weekSet) {
                values.add(weekRevenueMap.getOrDefault(week, 0));
            }
        } else if (period.equals("monthly")) {
            // Doanh thu theo tháng trong năm
            Map<Integer, Integer> monthRevenueMap = new HashMap<>();

            for (Object[] row : rawData) {
                System.out.println(Arrays.toString(row));
                int monthValue = ((Number) row[0]).intValue();  // row[0] là tháng
                int revenue = ((Number) row[1]).intValue();
                monthRevenueMap.put(monthValue, revenue);
            }

            for (int i = 1; i <= 12; i++) {
                values.add(monthRevenueMap.getOrDefault(i, 0));
            }
        } else if (period.equals("yearly")) {
            // Doanh thu theo năm
            Map<Integer, Integer> yearRevenueMap = new HashMap<>();

            for (Object[] row : rawData) {
                int revenueYear = ((Number) row[0]).intValue();  // row[0] là năm
                int revenue = ((Number) row[1]).intValue();
                yearRevenueMap.put(revenueYear, revenue);
            }

            List<Integer> sortedYears = new ArrayList<>(yearRevenueMap.keySet());
            Collections.sort(sortedYears);

            for (Integer y : sortedYears) {
                values.add(yearRevenueMap.getOrDefault(y, 0));
            }
        }

        return values;
    }



    public List<TopProductDTO> getTopProductsByQuantity(int month, int year,long storeId) {
        List<Bill> bills = dashboardRepository.findBillsByMonthAndYear(month, year,storeId);
        Map<String, TopProductDTO> productMap = new HashMap<>();

        for (Bill bill : bills) {
            List<ProductDataDTO> products = bill.getProducts();
            if (products != null) {
                for (ProductDataDTO product : products) {
                    String name = product.getName();
                    int quantity = product.getQuantity();
                    double price = product.getPrice();
                    double revenue = quantity * price;

                    TopProductDTO existing = productMap.get(name);
                    if (existing != null) {
                        existing.setTotalQuantity(existing.getTotalQuantity() + quantity);
                        existing.setTotalRevenue(existing.getTotalRevenue() + revenue);
                    } else {
                        productMap.put(name, new TopProductDTO(name, quantity, revenue));
                    }
                }
            }
        }

        // Sort theo quantity giảm dần
        List<TopProductDTO> result = new ArrayList<>(productMap.values());
        result.sort(Comparator.comparingInt(TopProductDTO::getTotalQuantity).reversed());

        return result.size() > 10 ? result.subList(0, 10) : result;
    }

    public List<Object[]> getRevenueByDayInMonth(Long storeId,int month, int year) {
        return dashboardRepository.getRevenueByDayInMonth(storeId,month, year);
    }

    public List<Object[]> getRevenueByMonthInYear(int year,long storeId) {
        return dashboardRepository.getRevenueByMonthInYear(year,storeId);
    }

    public List<Object[]> getRevenueByYearRange(int startYear, int endYear) {
        return dashboardRepository.getRevenueByYearRange(startYear, endYear);
    }


    public Page<Bill> getBillsToday(Long storeId, Pageable pageable) {
        return dashboardRepository.findTodayBills(storeId, pageable);
    }


}
