package com.goodee.beedan.controller.root;

import com.goodee.beedan.service.root.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/root/dashboard")
    public String dashboard(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer year,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer month,
            Model model) {
        LocalDate now = LocalDate.now();
        int y = (year != null) ? year : now.getYear();
        int m = (month != null) ? month : now.getMonthValue();

        YearMonth ym = YearMonth.of(y, m);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.atEndOfMonth().atTime(23, 59, 59);

        model.addAttribute("year", y);
        model.addAttribute("month", m);
        model.addAttribute("periodLabel", y + "년 " + m + "월");

        model.addAttribute("funnel", dashboardService.getFunnel(from, to));
        model.addAttribute("popularProducts", dashboardService.getPopularProducts(from, to, 10));
        model.addAttribute("conversionRanking", dashboardService.getConversionRanking(from, to));
        model.addAttribute("quoteStatus", dashboardService.getQuoteStatusCounts(from, to));
        model.addAttribute("monthlySignups", dashboardService.getMonthlySignups(y, m));
        model.addAttribute("weekdayQuotes", dashboardService.getQuotesByDayOfWeek(from, to));
        model.addAttribute("newSignups", dashboardService.getNewSignups(from, to));
        model.addAttribute("hourlyTraffic", dashboardService.getHourlyTraffic(from, to));
        model.addAttribute("sessionStats", dashboardService.getSessionStats(from, to));
        model.addAttribute("pageDwell", dashboardService.getPageDwellTime(from, to));
        model.addAttribute("inquiryStats", dashboardService.getInquiryStats(from, to));

        return "root/dashboard/root-dashboard";
    }
}
