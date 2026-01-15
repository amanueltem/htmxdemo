package com.aman.htmxdemo.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    // 1. CUMULATIVE: Sum every AUTHORIZED expense ever made BEFORE this month
    @Query("""
        SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e 
        JOIN User u ON e.inputter = u.email 
        WHERE u.groupMember.id = :groupId 
        AND e.date < :startDate 
        AND e.entityStatus = 'AUTHORIZED'
    """)
    Double sumAuthorizedBefore(@Param("groupId") Long groupId, @Param("startDate") LocalDate startDate);

    // 2. PERIOD: Sum strictly AUTHORIZED expenses for this month
    @Query("""
        SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e 
        JOIN User u ON e.inputter = u.email 
        WHERE u.groupMember.id = :groupId 
        AND e.date >= :startDate AND e.date <= :endDate 
        AND e.entityStatus = 'AUTHORIZED'
    """)
    Double sumAuthorizedInPeriod(@Param("groupId") Long groupId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    // 3. PIE CHART: Authorized only
    @Query("""
        SELECT e.timeSpan, SUM(e.amount) FROM Expense e 
        JOIN User u ON e.inputter = u.email 
        WHERE u.groupMember.id = :groupId 
        AND e.date >= :startDate AND e.date <= :endDate 
        AND e.entityStatus = 'AUTHORIZED'
        GROUP BY e.timeSpan
    """)
    List<Object[]> getAuthorizedCategorySumsRaw(@Param("groupId") Long groupId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    default Map<String, Double> getAuthorizedCategorySums(Long groupId, LocalDate startDate, LocalDate endDate) {
        return getAuthorizedCategorySumsRaw(groupId, startDate, endDate).stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).doubleValue(),
                        (v1, v2) -> v1
                ));
    }
}