package com.aman.htmxdemo.chart;

import java.util.Map;
public record ReportChart(
        Double openingBalance,      // Cumulative fund (Auth only) before this month
        Double monthDeposits,       // Authorized additions this month
        Double monthExpenses,       // Authorized spending this month
        Double availableBalance,    // The "One Truth" balance
        Map<String, Double> categoryBreakdown
) {}
