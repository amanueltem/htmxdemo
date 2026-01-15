package com.aman.htmxdemo.deposit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.UUID;

public interface DepositRepository extends JpaRepository<Deposit, UUID> {

    // 1. CUMULATIVE: All confirmed deposits from history before this month
    @Query("""
        SELECT COALESCE(SUM(d.amount), 0.0) FROM Deposit d 
        JOIN User u ON d.inputter = u.email 
        WHERE u.groupMember.id = :groupId 
        AND d.date < :startDate 
        AND d.entityStatus = 'AUTHORIZED'
    """)
    Double sumAuthorizedBefore(@Param("groupId") Long groupId, @Param("startDate") LocalDate startDate);

    // 2. PERIOD: Confirmed deposits for this month only
    @Query("""
        SELECT COALESCE(SUM(d.amount), 0.0) FROM Deposit d 
        JOIN User u ON d.inputter = u.email 
        WHERE u.groupMember.id = :groupId 
        AND d.date >= :startDate AND d.date <= :endDate 
        AND d.entityStatus = 'AUTHORIZED'
    """)
    Double sumAuthorizedInPeriod(@Param("groupId") Long groupId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
}