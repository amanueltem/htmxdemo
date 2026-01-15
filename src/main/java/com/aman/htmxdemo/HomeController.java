package com.aman.htmxdemo;
import com.aman.htmxdemo.chart.ReportChart;
import com.aman.htmxdemo.chart.ReportService;
import com.aman.htmxdemo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ReportService reportService;
    @GetMapping("/")
    public String index(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User user = (User) auth.getPrincipal();
            LocalDate now = LocalDate.now().withDayOfMonth(1);

            // Add the report here so the initial page load has data!
            ReportChart report = reportService.getRoomReport(user, now);
            model.addAttribute("report", report);
            model.addAttribute("selectedDate", now.toString().substring(0, 7));
        }
        return "index";
    }
}