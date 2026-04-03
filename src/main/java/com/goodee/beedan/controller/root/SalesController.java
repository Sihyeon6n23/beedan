package com.goodee.beedan.controller.root;

import com.goodee.beedan.common.constant.QuoteStatus;
import com.goodee.beedan.dto.root.sales.*;
import com.goodee.beedan.service.root.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @GetMapping("/root/sales")
    public String getSales(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model
    ) {
        // 기본값: 현재 월
        LocalDate now = LocalDate.now();
        int y = (year != null) ? year : now.getYear();
        int m = (month != null) ? month : now.getMonthValue();

        YearMonth ym = YearMonth.of(y, m);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.atEndOfMonth().atTime(23, 59, 59);

        // 기간 정보
        model.addAttribute("year", y);
        model.addAttribute("month", m);
        model.addAttribute("periodLabel", y + "년 " + m + "월");
        model.addAttribute("periodRange",
                ym.atDay(1).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        + " ~ "
                        + ym.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));

        // KPI
        SalesKpiResponse kpi = salesService.getKpi(from, to);
        model.addAttribute("kpi", kpi);

        // 수수료 요약
        Map<String, BigDecimal> commSummary = salesService.getCommissionSummary(from, to);
        model.addAttribute("commSummary", commSummary);

        // 수수료 매출 내역
        List<CommissionRow> commRows = salesService.getCommissionRows(from, to);
        model.addAttribute("commRows", commRows);

        // 수수료 합계 계산
        BigDecimal commItemTotal = commRows.stream()
                .map(CommissionRow::getItemAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commServiceTotal = commRows.stream()
                .map(CommissionRow::getServiceFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commShippingTotal = commRows.stream()
                .map(CommissionRow::getShippingFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commFeeTotal = commRows.stream()
                .map(CommissionRow::getTotalFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("commItemTotal", commItemTotal);
        model.addAttribute("commServiceTotal", commServiceTotal);
        model.addAttribute("commShippingTotal", commShippingTotal);
        model.addAttribute("commFeeTotal", commFeeTotal);

        // 등급별 분포
        List<GradeDistribution> gradeDist = salesService.getGradeDistribution(from, to);
        model.addAttribute("gradeDist", gradeDist);

        // 상태별 건수
        Map<QuoteStatus, Long> statusCounts = salesService.getStatusCounts(from, to);
        model.addAttribute("statusCounts", statusCounts);

        // 최근 견적 목록
        List<RecentQuoteRow> recentQuotes = salesService.getRecentQuotes(from, to, 10);
        model.addAttribute("recentQuotes", recentQuotes);

        return "/root/sales/root-sales";
    }
}
