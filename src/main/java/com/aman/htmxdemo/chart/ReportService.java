package com.aman.htmxdemo.chart;

import com.aman.htmxdemo.deposit.DepositRepository;
import com.aman.htmxdemo.expense.ExpenseRepository;
import com.aman.htmxdemo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

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
        Double prevDep = depositRepo.sumAuthorizedBefore(groupId, start);
        Double prevExp = expenseRepo.sumAuthorizedBefore(groupId, start);
        Double openingBalance = prevDep - prevExp;

        // 2. Get this month's confirmed activity
        Double monthDep = depositRepo.sumAuthorizedInPeriod(groupId, start, end);
        Double monthExp = expenseRepo.sumAuthorizedInPeriod(groupId, start, end);

        // 3. Final single balance calculation
        Double finalFund = (openingBalance + monthDep) - monthExp;

        Map<String, Double> categories = expenseRepo.getAuthorizedCategorySums(groupId, start, end);

        return new ReportChart(
                openingBalance,
                monthDep,
                monthExp,
                finalFund,
                categories
        );
    }
}