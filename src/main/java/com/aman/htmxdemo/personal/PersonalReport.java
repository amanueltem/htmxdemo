package com.aman.htmxdemo.personal;

import java.util.Map;

public record PersonalReport(
        Double totalSpent,
        Map<String, Double> categoryBreakdown,
        String selectedMonth,
        boolean hasNoData
) {}
