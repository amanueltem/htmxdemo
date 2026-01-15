package com.aman.htmxdemo.chart;

import java.util.Map;
public record ReportChart(
        Double openingBalance,
        Double monthDeposits,
        Double monthExpenses,
        Double availableBalance,
        Map<String, Double> categoryBreakdown,
        // Helper fields to avoid reflection in HTML
        boolean hasNoData,
        boolean isNegative,
        String formattedBalance
) {}