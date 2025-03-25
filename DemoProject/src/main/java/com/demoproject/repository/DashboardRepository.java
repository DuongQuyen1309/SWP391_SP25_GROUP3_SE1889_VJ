package com.demoproject.repository;

import com.demoproject.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DashboardRepository extends JpaRepository<Bill,Long> {
    @Query("SELECT COUNT(b) FROM Bill b " +
            "WHERE CAST(b.createdAt AS date) = CAST(CURRENT_DATE AS date) " +
            "AND b.storeId = :storeId")
    Long getTodayBillCount(@Param("storeId") Long storeId);


    @Query("SELECT SUM(b.totalMoney) FROM Bill b " +
            "WHERE CAST(b.createdAt AS date) = CAST(CURRENT_DATE AS date) " +
            "AND b.storeId = :storeId")
    Long getTodayRevenue(@Param("storeId") Long storeId);


    @Query("SELECT COUNT(b) FROM Bill b " +
            "WHERE CAST(b.createdAt AS date) = :date " +
            "AND b.storeId = :storeId")
    Long getBillCountByDate(@Param("date") LocalDate date, @Param("storeId") Long storeId);

    @Query("SELECT SUM(b.totalMoney) FROM Bill b " +
            "WHERE CAST(b.createdAt AS date) = :date " +
            "AND b.storeId = :storeId")
    Long getRevenueByDate(@Param("date") LocalDate date, @Param("storeId") Long storeId);



    @Query("SELECT SUM(b.totalMoney) FROM Bill b WHERE b.storeId = :storeId")
    Long getTotalRevenue(@Param("storeId") Long storeId);
    // Nếu `status = true` là hóa đơn hợp lệ

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



    @Query("SELECT b FROM Bill b " +
            "WHERE MONTH(b.createdAt) = :month " +
            "AND YEAR(b.createdAt) = :year " +
            "AND b.storeId = :storeId")
    List<Bill> findBillsByMonthAndYear(@Param("month") int month,
                                       @Param("year") int year,
                                       @Param("storeId") Long storeId);

    @Query("SELECT DAY(b.createdAt), SUM(b.totalMoney) " +
            "FROM Bill b " +
            "WHERE MONTH(b.createdAt) = :month " +
            "AND YEAR(b.createdAt) = :year " +
            "AND b.storeId = :storeId " +  // Lọc theo storeId
            "GROUP BY DAY(b.createdAt) " +
            "ORDER BY DAY(b.createdAt)")
    List<Object[]> getRevenueByDayInMonth(@Param("storeId") Long storeId,
                                          @Param("month") int month,
                                          @Param("year") int year);


    @Query("SELECT MONTH(b.createdAt), SUM(b.totalMoney) " +
            "FROM Bill b " +
            "WHERE YEAR(b.createdAt) = :year " +
            "AND b.storeId = :storeId " +
            "GROUP BY MONTH(b.createdAt) " +
            "ORDER BY MONTH(b.createdAt)")
    List<Object[]> getRevenueByMonthInYear(@Param("year") int year, @Param("storeId") Long storeId);


    @Query(value = """
    WITH YearSeries AS (
        SELECT :startYear AS year
        UNION ALL
        SELECT year + 1 FROM YearSeries WHERE year < :endYear
    )
    SELECT ys.year, COALESCE(SUM(b.totalMoney), 0) AS totalRevenue
    FROM YearSeries ys
    LEFT JOIN Bill b ON YEAR(b.createdAt) = ys.year
    GROUP BY ys.year
    ORDER BY ys.year ASC
""", nativeQuery = true)
    List<Object[]> getRevenueByYearRange(@Param("startYear") int startYear,
                                         @Param("endYear") int endYear);

    @Query("SELECT SUM(b.totalMoney) FROM Bill b WHERE b.storeId = :storeId AND FUNCTION('YEAR', b.createdAt) = :year AND FUNCTION('MONTH', b.createdAt) = :month")
    Integer getRevenueByMonth(@Param("storeId") Long storeId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(b.totalMoney) FROM Bill b WHERE b.storeId = :storeId AND FUNCTION('YEAR', b.createdAt) = :year")
    Integer getRevenueByYear(@Param("storeId") Long storeId, @Param("year") int year);

    @Query("SELECT FUNCTION('YEAR', b.createdAt) AS year, SUM(b.totalMoney) FROM Bill b WHERE b.storeId = :storeId GROUP BY FUNCTION('YEAR', b.createdAt) ORDER BY year DESC")
    List<Object[]> getRevenueLastFiveYears(@Param("storeId") Long storeId);


    @Query("SELECT b FROM Bill b WHERE CAST(b.createdAt AS date) = CURRENT_DATE AND b.storeId = :storeId")
    List<Bill> findBillsByToday(@Param("storeId") Long storeId);
    @Query("SELECT b FROM Bill b WHERE b.storeId = :storeId AND CAST(b.createdAt AS date) = CAST(GETDATE() AS date)")
    Page<Bill> findTodayBills(@Param("storeId") Long storeId, Pageable pageable);


}
