package com.goodee.beedan.controller.root;

import com.goodee.beedan.common.constant.QuoteStatus;
import com.goodee.beedan.dto.root.sales.*;
import com.goodee.beedan.service.pdf.SalesPdfService;
import com.goodee.beedan.service.root.SalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@Slf4j
@Controller
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;
    private final SalesPdfService salesPdfService;

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

        SalesKpiResponse kpi = salesService.getKpi(from, to);
        model.addAttribute("kpi", kpi);

        Map<String, BigDecimal> commSummary = salesService.getCommissionSummary(from, to);
        model.addAttribute("commSummary", commSummary);

        List<CommissionRow> commRows = salesService.getCommissionRows(from, to);
        model.addAttribute("commRows", commRows);

        BigDecimal commItemTotal = commRows.stream()
                .map(CommissionRow::getItemAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commServiceTotal = commRows.stream()
                .map(CommissionRow::getServiceFee)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commShippingTotal = commRows.stream()
                .map(CommissionRow::getShippingFee)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commFeeTotal = commRows.stream()
                .map(CommissionRow::getTotalFee)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("commItemTotal", commItemTotal);
        model.addAttribute("commServiceTotal", commServiceTotal);
        model.addAttribute("commShippingTotal", commShippingTotal);
        model.addAttribute("commFeeTotal", commFeeTotal);

        List<GradeDistribution> gradeDist = salesService.getGradeDistribution(from, to);
        model.addAttribute("gradeDist", gradeDist);

        model.addAttribute("monthlyTrend", salesService.getMonthlyTrend(y, m));
        try { log.info("[Sales] 7. 모든 데이터 로드 완료"); }catch(Exception e){}

        return "/root/sales/root-sales";
    }

    @GetMapping("/root/sales/pdf")
    public ResponseEntity<byte[]> downloadSalesPdf(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        java.time.LocalDate now = java.time.LocalDate.now();
        int y = (year != null) ? year : now.getYear();
        int m = (month != null) ? month : now.getMonthValue();

        try {
            byte[] pdf = salesPdfService.generateSalesPdf(y, m);
            String filename = "sales-report-" + y + "-" + String.format("%02d", m) + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
