package com.aman.htmxdemo.chart;

import com.aman.htmxdemo.deposit.DepositRepository;
import com.aman.htmxdemo.expense.ExpenseRepository;
import com.aman.htmxdemo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ExpenseRepository expenseRepo;
    private final DepositRepository depositRepo;

    public ReportChart getRoomReport(User user, LocalDate selectedMonth) {
        Long groupId = user.getGroupMember().getId();
        LocalDate start = selectedMonth.withDayOfMonth(1);
        LocalDate end = selectedMonth.withDayOfMonth(selectedMonth.lengthOfMonth());

        // 1. Get the "Carry Over" from all previous history
        // We use .getOrDefault to prevent NullPointerExceptions in Native Mode
        Double prevDep = Objects.requireNonNullElse(depositRepo.sumAuthorizedBefore(groupId, start), 0.0);
        Double prevExp = Objects.requireNonNullElse(expenseRepo.sumAuthorizedBefore(groupId, start), 0.0);
        Double openingBalance = prevDep - prevExp;

        // 2. Get this month's confirmed activity
        Double monthDep = Objects.requireNonNullElse(depositRepo.sumAuthorizedInPeriod(groupId, start, end), 0.0);
        Double monthExp = Objects.requireNonNullElse(expenseRepo.sumAuthorizedInPeriod(groupId, start, end), 0.0);

        // 3. Final single balance calculation
        Double finalFund = (openingBalance + monthDep) - monthExp;

        Map<String, Double> categories = expenseRepo.getAuthorizedCategorySums(groupId, start, end);

        // 4. Pre-calculate Helper Fields (The "Deep Build" Fix)
        boolean hasNoData = categories == null || categories.isEmpty();
        boolean isNegative = finalFund < 0;

        // Manual formatting replaces Thymeleaf's #numbers utility to avoid reflection
        String formattedBalance = String.format("%.2f ETB", finalFund);

        return new ReportChart(
                openingBalance,
                monthDep,
                monthExp,
                finalFund,
                categories,
                hasNoData,      // Maps to th:if="${report.hasNoData()}"
                isNegative,     // Maps to th:classappend="${report.isNegative()}"
                formattedBalance // Maps to th:text="${report.formattedBalance()}"
        );
    }
}