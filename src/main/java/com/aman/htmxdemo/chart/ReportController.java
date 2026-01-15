package com.aman.htmxdemo.chart;

import com.aman.htmxdemo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    @GetMapping
    public String getReport(@RequestParam(required = false) String date, Model model, Authentication auth) {
        User user = (User) auth.getPrincipal();

        // Default to current month if no date is picked
        LocalDate selectedDate = (date != null)
                ? LocalDate.parse(date + "-01")
                : LocalDate.now().withDayOfMonth(1);

        ReportChart report = reportService.getRoomReport(user, selectedDate);

        model.addAttribute("report", report);
        model.addAttribute("selectedDate", selectedDate.toString().substring(0, 7)); // yyyy-MM
        return "index :: report-chart";
    }
}
