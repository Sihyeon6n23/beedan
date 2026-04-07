package com.goodee.beedan.service.root;

import com.goodee.beedan.common.constant.QuoteStatus;
import com.goodee.beedan.dto.root.sales.*;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.buyer.BuyerRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.payment.PaymentRepository;
import com.goodee.beedan.repository.quote.QuoteBaseRepository;
import com.goodee.beedan.repository.quote.QuoteDetailRepository;
import com.goodee.beedan.repository.quote.QuoteInfoRepository;
import com.goodee.beedan.service.quote.NegotiationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesService {

    private final QuoteBaseRepository quoteBaseRepository;
    private final QuoteInfoRepository quoteInfoRepository;
    private final QuoteDetailRepository quoteDetailRepository;
    private final BuyerRepository buyerRepository;
    private final com.goodee.beedan.repository.buyer.BuyerGradePolicyRepository buyerGradePolicyRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final NegotiationService negotiationService;

    /**
     * KPI 카드 데이터 조회
     */
    public SalesKpiResponse getKpi(LocalDateTime from, LocalDateTime to) {
        // PAID 기준 매출 집계
        List<QuoteBase> paidQuotes = quoteBaseRepository
                .findAllByQuSttAndQuCreDtBetween(QuoteStatus.PAID, from, to);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (QuoteBase qb : paidQuotes) {
            Payment payment = paymentRepository.findByQuId(qb.getQuId()).orElse(null);
            if (payment != null && payment.getPyTtAm() != null) {
                totalRevenue = totalRevenue.add(payment.getPyTtAm());
            }
        }

        long totalOrders = paidQuotes.size();
        BigDecimal avgOrderAmount = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long newCustomers = buyerRepository.countByByFrDtBetween(from, to);

        return SalesKpiResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .avgOrderAmount(avgOrderAmount)
                .newCustomers(newCustomers)
                .build();
    }

    /**
     * 수수료 매출 내역 (승인된 견적 기준)
     */
    public List<CommissionRow> getCommissionRows(LocalDateTime from, LocalDateTime to) {
        List<QuoteBase> approvedQuotes = quoteBaseRepository
                .findAllByQuSttAndQuCreDtBetween(QuoteStatus.PAID, from, to);

        List<CommissionRow> rows = new ArrayList<>();
        for (QuoteBase qb : approvedQuotes) {
            QuoteInfo info = quoteInfoRepository.findByQuId(qb.getQuId()).orElse(null);
            if (info == null) continue;

            // 물품 대금 = 총합계 - 배송비 - 서비스수수료 - 관부가세
            BigDecimal itemAmount = calculateItemAmount(info);
            BigDecimal serviceFee = nullToZero(info.getQuInfoSrvFeAm());
            BigDecimal shippingFee = nullToZero(info.getQuInfoTtlShiFe());
            BigDecimal totalFee = serviceFee.add(shippingFee);

            // 바이어 정보 조회 (협상의 고객 기준)
            Long customerId = null;
            try {
                Negotiation ng = negotiationService.findById(qb.getNgId());
                customerId = ng.getMemId();
            } catch (Exception ignored) {}
            String buyerName = resolveBuyerName(customerId);
            String grade = resolveGradeByNego(qb.getNgId());

            rows.add(CommissionRow.builder()
                    .date(qb.getQuCreDt())
                    .quoteCd(qb.getQuCd())
                    .buyerName(buyerName)
                    .grade(grade)
                    .itemAmount(itemAmount)
                    .serviceFee(serviceFee)
                    .shippingFee(shippingFee)
                    .totalFee(totalFee)
                    .build());
        }

        rows.sort(Comparator.comparing(CommissionRow::getDate).reversed());
        return rows;
    }

    /**
     * 수수료 합계 (서비스/배송/통관)
     */
    public Map<String, BigDecimal> getCommissionSummary(LocalDateTime from, LocalDateTime to) {
        List<QuoteBase> approvedQuotes = quoteBaseRepository
                .findAllByQuSttAndQuCreDtBetween(QuoteStatus.PAID, from, to);

        BigDecimal totalServiceFee = BigDecimal.ZERO;
        BigDecimal totalShippingFee = BigDecimal.ZERO;
        BigDecimal totalCustomsFee = BigDecimal.ZERO;
        long serviceCount = 0;
        long shippingCount = 0;
        long customsCount = 0;

        for (QuoteBase qb : approvedQuotes) {
            QuoteInfo info = quoteInfoRepository.findByQuId(qb.getQuId()).orElse(null);
            if (info == null) continue;

            if (info.getQuInfoSrvFeAm() != null && info.getQuInfoSrvFeAm().compareTo(BigDecimal.ZERO) > 0) {
                totalServiceFee = totalServiceFee.add(info.getQuInfoSrvFeAm());
                serviceCount++;
            }
            if (info.getQuInfoTtlShiFe() != null && info.getQuInfoTtlShiFe().compareTo(BigDecimal.ZERO) > 0) {
                totalShippingFee = totalShippingFee.add(info.getQuInfoTtlShiFe());
                shippingCount++;
            }
            if (info.getQuInfoTax() != null && info.getQuInfoTax().compareTo(BigDecimal.ZERO) > 0) {
                totalCustomsFee = totalCustomsFee.add(info.getQuInfoTax());
                customsCount++;
            }
        }

        Map<String, BigDecimal> summary = new LinkedHashMap<>();
        summary.put("serviceFee", totalServiceFee);
        summary.put("serviceCount", BigDecimal.valueOf(serviceCount));
        summary.put("shippingFee", totalShippingFee);
        summary.put("shippingCount", BigDecimal.valueOf(shippingCount));
        summary.put("customsFee", totalCustomsFee);
        summary.put("customsCount", BigDecimal.valueOf(customsCount));
        return summary;
    }

    /**
     * 등급별 매출 분포
     */
    public List<GradeDistribution> getGradeDistribution(LocalDateTime from, LocalDateTime to) {
        List<QuoteBase> approvedQuotes = quoteBaseRepository
                .findAllByQuSttAndQuCreDtBetween(QuoteStatus.PAID, from, to);

        // 등급별 집계
        Map<String, BigDecimal> revenueByGrade = new LinkedHashMap<>();
        Map<String, Long> ordersByGrade = new LinkedHashMap<>();
        Map<String, Set<Long>> buyersByGrade = new LinkedHashMap<>();

        // DB에서 활성 등급 목록 조회
        List<String> grades = buyerGradePolicyRepository.findAllByBgpAcYnTrue().stream()
                .map(com.goodee.beedan.entity.BuyerGradePolicy::getBgpGr)
                .distinct()
                .collect(Collectors.toList());
        if (grades.isEmpty()) grades = List.of("STANDARD");

        for (String grade : grades) {
            revenueByGrade.put(grade, BigDecimal.ZERO);
            ordersByGrade.put(grade, 0L);
            buyersByGrade.put(grade, new HashSet<>());
        }

        for (QuoteBase qb : approvedQuotes) {
            Payment payment = paymentRepository.findByQuId(qb.getQuId()).orElse(null);
            if (payment == null) continue;

            String grade = resolveGradeByNego(qb.getNgId());
            if (grade == null) grade = "STANDARD";

            revenueByGrade.merge(grade, nullToZero(payment.getPyTtAm()), BigDecimal::add);
            ordersByGrade.merge(grade, 1L, Long::sum);
            Long custId = null;
            try { custId = negotiationService.findById(qb.getNgId()).getMemId(); } catch (Exception ignored) {}
            buyersByGrade.computeIfAbsent(grade, k -> new HashSet<>()).add(custId);
        }

        BigDecimal totalRevenue = revenueByGrade.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<GradeDistribution> result = new ArrayList<>();
        for (String grade : grades) {
            BigDecimal revenue = revenueByGrade.get(grade);
            BigDecimal pct = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? revenue.multiply(BigDecimal.valueOf(100))
                    .divide(totalRevenue, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            result.add(GradeDistribution.builder()
                    .grade(grade)
                    .revenue(revenue)
                    .orderCount(ordersByGrade.get(grade))
                    .buyerCount(buyersByGrade.get(grade).size())
                    .percentage(pct)
                    .build());
        }
        return result;
    }

    /**
     * 최근 견적 현황
     */
    public List<RecentQuoteRow> getRecentQuotes(LocalDateTime from, LocalDateTime to, int limit) {
        List<QuoteBase> quotes = quoteBaseRepository
                .findAllByQuCreDtBetweenOrderByQuCreDtDesc(from, to);

        return quotes.stream()
                .filter(qb -> qb.getQuStt() != null && !QuoteStatus.TEMP_SAVE.equals(qb.getQuStt()))
                .limit(limit)
                .map(qb -> {
                    // PAID면 Payment에서, 아니면 0
                    Payment payment = paymentRepository.findByQuId(qb.getQuId()).orElse(null);
                    BigDecimal amount = (payment != null) ? nullToZero(payment.getPyTtAm()) : BigDecimal.ZERO;

                    Long customerId = null;
                    try { customerId = negotiationService.findById(qb.getNgId()).getMemId(); } catch (Exception ignored) {}

                    return RecentQuoteRow.builder()
                            .quoteCd(qb.getQuCd())
                            .buyerName(resolveBuyerName(customerId))
                            .status(qb.getQuStt())
                            .amount(amount)
                            .submitDate(qb.getQuCreDt())
                            .curatorName(resolveCuratorName(qb.getQuSid()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 상태별 견적 건수
     */
    public Map<QuoteStatus, Long> getStatusCounts(LocalDateTime from, LocalDateTime to) {
        Map<QuoteStatus, Long> counts = new LinkedHashMap<>();
        for (QuoteStatus status : QuoteStatus.values()) {
            counts.put(status, quoteBaseRepository.countByQuSttAndQuCreDtBetween(status, from, to));
        }
        return counts;
    }

    // ===== private helpers =====

    private BigDecimal calculateItemAmount(QuoteInfo info) {
        BigDecimal total = nullToZero(info.getQuInfoTp());
        BigDecimal shipping = nullToZero(info.getQuInfoTtlShiFe());
        BigDecimal service = nullToZero(info.getQuInfoSrvFeAm());
        BigDecimal tax = nullToZero(info.getQuInfoTax());
        BigDecimal item = total.subtract(shipping).subtract(service).subtract(tax);
        return item.compareTo(BigDecimal.ZERO) >= 0 ? item : BigDecimal.ZERO;
    }

    private String resolveBuyerName(Long buyerId) {
        if (buyerId == null) return "-";
        return memberRepository.findById(buyerId)
                .map(Member::getMemBizTtl)
                .orElse("-");
    }

    private String resolveBuyerGrade(Long buyerId) {
        if (buyerId == null) return "STANDARD";
        return memberRepository.findById(buyerId)
                .map(Member::getMemBizNo)
                .flatMap(buyerRepository::findByMemBizNo)
                .map(Buyer::getBgpGr)
                .orElse("STANDARD");
    }

    /** 협상의 고객 등급 조회 (negotiation.memId 기준) */
    private String resolveGradeByNego(Long ngId) {
        if (ngId == null) return "STANDARD";
        try {
            Negotiation ng = negotiationService.findById(ngId);
            return resolveBuyerGrade(ng.getMemId());
        } catch (Exception e) {
            return "STANDARD";
        }
    }

    private String resolveCuratorName(Long curatorId) {
        if (curatorId == null) return "-";
        return memberRepository.findById(curatorId)
                .map(Member::getMemNm)
                .orElse("-");
    }

    private BigDecimal nullToZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
