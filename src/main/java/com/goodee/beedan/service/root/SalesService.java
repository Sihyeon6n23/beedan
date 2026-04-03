package com.goodee.beedan.service.root;

import com.goodee.beedan.common.constant.QuoteStatus;
import com.goodee.beedan.dto.root.sales.*;
import com.goodee.beedan.entity.Buyer;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.QuoteBase;
import com.goodee.beedan.entity.QuoteInfo;
import com.goodee.beedan.repository.buyer.BuyerRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.quote.QuoteBaseRepository;
import com.goodee.beedan.repository.quote.QuoteDetailRepository;
import com.goodee.beedan.repository.quote.QuoteInfoRepository;
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
    private final MemberRepository memberRepository;

    /**
     * KPI 카드 데이터 조회
     */
    public SalesKpiResponse getKpi(LocalDateTime from, LocalDateTime to) {
        List<QuoteBase> approvedQuotes = quoteBaseRepository
                .findAllByQuSttAndQuCreDtBetween(QuoteStatus.APPROVED, from, to);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (QuoteBase qb : approvedQuotes) {
            QuoteInfo info = quoteInfoRepository.findByQuId(qb.getQuId()).orElse(null);
            if (info != null && info.getQuInfoTp() != null) {
                totalRevenue = totalRevenue.add(info.getQuInfoTp());
            }
        }

        long totalOrders = approvedQuotes.size();
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
                .findAllByQuSttAndQuCreDtBetween(QuoteStatus.APPROVED, from, to);

        List<CommissionRow> rows = new ArrayList<>();
        for (QuoteBase qb : approvedQuotes) {
            QuoteInfo info = quoteInfoRepository.findByQuId(qb.getQuId()).orElse(null);
            if (info == null) continue;

            // 물품 대금 = 총합계 - 배송비 - 서비스수수료 - 관부가세
            BigDecimal itemAmount = calculateItemAmount(info);
            BigDecimal serviceFee = nullToZero(info.getQuInfoSrvFeAm());
            BigDecimal shippingFee = nullToZero(info.getQuInfoTtlShiFe());
            BigDecimal totalFee = serviceFee.add(shippingFee);

            // 바이어 정보 조회
            String buyerName = resolveBuyerName(qb.getQuRid());
            String grade = resolveBuyerGrade(qb.getQuRid());

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
                .findAllByQuSttAndQuCreDtBetween(QuoteStatus.APPROVED, from, to);

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
                .findAllByQuSttAndQuCreDtBetween(QuoteStatus.APPROVED, from, to);

        // 등급별 집계
        Map<String, BigDecimal> revenueByGrade = new LinkedHashMap<>();
        Map<String, Long> ordersByGrade = new LinkedHashMap<>();
        Map<String, Set<Long>> buyersByGrade = new LinkedHashMap<>();

        for (String grade : List.of("VIP", "PREMIUM", "STANDARD")) {
            revenueByGrade.put(grade, BigDecimal.ZERO);
            ordersByGrade.put(grade, 0L);
            buyersByGrade.put(grade, new HashSet<>());
        }

        for (QuoteBase qb : approvedQuotes) {
            QuoteInfo info = quoteInfoRepository.findByQuId(qb.getQuId()).orElse(null);
            if (info == null) continue;

            String grade = resolveBuyerGrade(qb.getQuRid());
            if (grade == null) grade = "STANDARD";

            revenueByGrade.merge(grade, nullToZero(info.getQuInfoTp()), BigDecimal::add);
            ordersByGrade.merge(grade, 1L, Long::sum);
            buyersByGrade.computeIfAbsent(grade, k -> new HashSet<>()).add(qb.getQuRid());
        }

        BigDecimal totalRevenue = revenueByGrade.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<GradeDistribution> result = new ArrayList<>();
        for (String grade : List.of("VIP", "PREMIUM", "STANDARD")) {
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
                .limit(limit)
                .map(qb -> {
                    QuoteInfo info = quoteInfoRepository.findByQuId(qb.getQuId()).orElse(null);
                    BigDecimal amount = (info != null) ? nullToZero(info.getQuInfoTp()) : BigDecimal.ZERO;

                    return RecentQuoteRow.builder()
                            .quoteCd(qb.getQuCd())
                            .buyerName(resolveBuyerName(qb.getQuRid()))
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
