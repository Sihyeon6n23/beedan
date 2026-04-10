package com.goodee.beedan.service.pdf;

import com.goodee.beedan.dto.root.sales.*;
import com.goodee.beedan.service.root.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SalesPdfService {

    private final SalesService salesService;
    private final TemplateEngine templateEngine;

    public byte[] generateSalesPdf(int year, int month) throws Exception {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.atEndOfMonth().atTime(23, 59, 59);

        SalesKpiResponse kpi = salesService.getKpi(from, to);
        Map<String, BigDecimal> commSummary = salesService.getCommissionSummary(from, to);
        List<CommissionRow> commRows = salesService.getCommissionRows(from, to);
        List<GradeDistribution> gradeDist = salesService.getGradeDistribution(from, to);

        BigDecimal commItemTotal = commRows.stream().map(CommissionRow::getItemAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commServiceTotal = commRows.stream().map(CommissionRow::getServiceFee).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commShippingTotal = commRows.stream().map(CommissionRow::getShippingFee).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commFeeTotal = commRows.stream().map(CommissionRow::getTotalFee).reduce(BigDecimal.ZERO, BigDecimal::add);

        String periodLabel = year + "년 " + month + "월";
        String periodRange = ym.atDay(1).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                + " ~ " + ym.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

        Context ctx = new Context();
        ctx.setVariable("periodLabel", periodLabel);
        ctx.setVariable("periodRange", periodRange);
        ctx.setVariable("kpi", kpi);
        ctx.setVariable("commSummary", commSummary);
        ctx.setVariable("commRows", commRows);
        ctx.setVariable("commItemTotal", commItemTotal);
        ctx.setVariable("commServiceTotal", commServiceTotal);
        ctx.setVariable("commShippingTotal", commShippingTotal);
        ctx.setVariable("commFeeTotal", commFeeTotal);
        ctx.setVariable("gradeDist", gradeDist);
        List<Map<String, Object>> trend = salesService.getMonthlyTrend(year, month);
        long trendMax = trend.stream()
                .map(t -> ((BigDecimal) t.get("total")).longValue())
                .max(Long::compare).orElse(1L);
        if (trendMax == 0) trendMax = 1;
        ctx.setVariable("monthlyTrend", trend);
        ctx.setVariable("trendMax", trendMax);

        String html = templateEngine.process("pdf/sales-pdf", ctx);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        String fontPath = getClass().getClassLoader().getResource("fonts/NotoSansKR-Regular.ttf").toExternalForm();
        renderer.getFontResolver().addFont(fontPath, "NotoSansKR",
                com.lowagie.text.pdf.BaseFont.IDENTITY_H, true, null);
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(baos);
        return baos.toByteArray();
    }
}
