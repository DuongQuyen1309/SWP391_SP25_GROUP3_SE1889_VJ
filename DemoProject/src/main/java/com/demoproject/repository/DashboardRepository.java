package com.demoproject.repository;

import com.demoproject.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DashboardRepository extends JpaRepository<Bill,Long> {
    @Query("SELECT COUNT(b) FROM Bill b WHERE CAST(b.createdAt AS date) = CAST(GETDATE() AS date)")
    Long getTodayBillCount();

    @Query("SELECT SUM(b.totalMoney) FROM Bill b WHERE CAST(b.createdAt AS date) = CAST(GETDATE() AS date)")
    Long getTodayRevenue();

    @Query("SELECT COUNT(b) FROM Bill b WHERE CAST(b.createdAt AS date) = :date")
    Long getBillCountByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(b.totalMoney) FROM Bill b WHERE CAST(b.createdAt AS date) = :date")
    Long getRevenueByDate(@Param("date") LocalDate date);


    @Query("SELECT SUM(b.totalMoney) FROM Bill b ")
    Long getTotalRevenue();  // Nếu `status = true` là hóa đơn hợp lệ

    @Query(value = "SELECT CONVERT(DATE, created_at) AS dateVal, SUM(total_money) AS totalRevenue " +
            "FROM bill " +
            "WHERE created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY CONVERT(DATE, created_at) " +
            "ORDER BY CONVERT(DATE, created_at)", nativeQuery = true)
    List<Object[]> getRevenueByDay(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);


    @Query("SELECT FUNCTION('YEAR', b.createdAt) AS yearValue, FUNCTION('MONTH', b.createdAt) AS monthValue, SUM(b.totalMoney) AS totalRevenue " +
            "FROM Bill b WHERE b.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', b.createdAt), FUNCTION('MONTH', b.createdAt) " +
            "ORDER BY FUNCTION('YEAR', b.createdAt), FUNCTION('MONTH', b.createdAt)")
    List<Object[]> getRevenueByMonth(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);



    @Query("SELECT FUNCTION('YEAR', b.createdAt) AS year, SUM(b.totalMoney) AS totalRevenue " +
            "FROM Bill b WHERE b.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', b.createdAt) ORDER BY FUNCTION('YEAR', b.createdAt)")
    List<Object[]> getRevenueByYear(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT FUNCTION('YEAR', b.createdAt), FUNCTION('WEEK', b.createdAt), SUM(b.totalMoney) " +
            "FROM Bill b WHERE b.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', b.createdAt), FUNCTION('WEEK', b.createdAt) " +
            "ORDER BY FUNCTION('YEAR', b.createdAt), FUNCTION('WEEK', b.createdAt)")
    List<Object[]> getRevenueByWeek(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT DAY(created_at) AS dayVal, SUM(total_money) AS totalRevenue " +
            "FROM bill " +
            "WHERE MONTH(created_at) = MONTH(GETDATE()) AND YEAR(created_at) = YEAR(GETDATE()) " +
            "GROUP BY DAY(created_at) ORDER BY DAY(created_at)", nativeQuery = true)
    List<Object[]> getRevenueDailyThisMonth();

    @Query("SELECT b FROM Bill b WHERE MONTH(b.createdAt) = :month AND YEAR(b.createdAt) = :year")
    List<Bill> findBillsByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT DAY(b.createdAt), SUM(b.totalMoney) " +
            "FROM Bill b WHERE MONTH(b.createdAt) = :month AND YEAR(b.createdAt) = :year " +
            "GROUP BY DAY(b.createdAt) ORDER BY DAY(b.createdAt)")
    List<Object[]> getRevenueByDayInMonth(@Param("month") int month, @Param("year") int year);

    @Query("SELECT MONTH(b.createdAt), SUM(b.totalMoney) " +
            "FROM Bill b WHERE YEAR(b.createdAt) = :year " +
            "GROUP BY MONTH(b.createdAt) ORDER BY MONTH(b.createdAt)")
    List<Object[]> getRevenueByMonthInYear(@Param("year") int year);
}
