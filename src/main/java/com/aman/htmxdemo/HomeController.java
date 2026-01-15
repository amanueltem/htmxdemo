package com.aman.htmxdemo;

import com.aman.htmxdemo.chart.ReportChart;
import com.aman.htmxdemo.chart.ReportService;
import com.aman.htmxdemo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ReportService reportService;

    @GetMapping("/")
    public String index(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User user = (User) auth.getPrincipal();

            // 1. Get current month (first day)
            LocalDate now = LocalDate.now().withDayOfMonth(1);

            // 2. Pre-load the report for the "Contagious Fund" dashboard
            ReportChart report = reportService.getRoomReport(user, now);

            // 3. Format the date as yyyy-MM (crucial for the HTML <input type="month">)
            String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            model.addAttribute("report", report);
            model.addAttribute("selectedDate", formattedDate);
        }
        return "index";
    }
}