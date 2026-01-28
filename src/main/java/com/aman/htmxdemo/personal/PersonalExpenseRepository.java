package com.aman.htmxdemo.personal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PersonalExpenseRepository extends JpaRepository<PersonalExpense, UUID> {
    Page<PersonalExpense> findByInputter(String inputter, Pageable pageable);
    @Query("""
        SELECT e.timeSpan, SUM(e.amount) FROM PersonalExpense e 
        WHERE e.inputter = :email 
        AND e.date >= :start AND e.date <= :end 
        GROUP BY e.timeSpan
    """)
    List<Object[]> getCategorySumsRaw(@Param("email") String email,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);
}
