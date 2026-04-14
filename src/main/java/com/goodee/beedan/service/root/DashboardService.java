package com.goodee.beedan.service.root;

import com.goodee.beedan.common.constant.QuoteStatus;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.repository.chat.ChatRoomRepository;
import com.goodee.beedan.entity.ChatRoom;
import com.goodee.beedan.repository.cart.CartRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.pageview.PageViewRepository;
import com.goodee.beedan.repository.payment.PaymentRepository;
import com.goodee.beedan.repository.quote.QuoteBaseRepository;
import com.goodee.beedan.repository.sessionlog.SessionLogRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final PageViewRepository pageViewRepository;
    private final CartRepository cartRepository;
    private final SessionLogRepository sessionLogRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final QuoteBaseRepository quoteBaseRepository;
    private final MemberRepository memberRepository;
    private final StockRepository stockRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 전환 퍼널 데이터
     */
    public Map<String, Object> getFunnel(LocalDateTime from, LocalDateTime to) {
        long views = pageViewRepository.countByPvPageAndPvCreDtBetween("STOCK_DETAIL", from, to);
        long carts = cartRepository.count(); // 현재 장바구니 총 건수 (누적)
        long quotes = quoteBaseRepository.countByQuSttAndQuCreDtBetween(QuoteStatus.SUBMITTED, from, to)
                + quoteBaseRepository.countByQuSttAndQuCreDtBetween(QuoteStatus.APPROVED, from, to)
                + quoteBaseRepository.countByQuSttAndQuCreDtBetween(QuoteStatus.REJECTED, from, to)
                + quoteBaseRepository.countByQuSttAndQuCreDtBetween(QuoteStatus.PAID, from, to);
        long paid = quoteBaseRepository.countByQuSttAndQuCreDtBetween(QuoteStatus.PAID, from, to);

        Map<String, Object> funnel = new LinkedHashMap<>();
        funnel.put("views", views);
        funnel.put("carts", carts);
        funnel.put("quotes", quotes);
        funnel.put("paid", paid);
        funnel.put("viewToCart", views > 0 ? Math.round((double) carts / views * 100) : 0);
        funnel.put("cartToQuote", carts > 0 ? Math.round((double) quotes / carts * 100) : 0);
        funnel.put("quoteToPaid", quotes > 0 ? Math.round((double) paid / quotes * 100) : 0);
        funnel.put("totalConversion", views > 0 ? Math.round((double) paid / views * 1000) / 10.0 : 0);
        return funnel;
    }

    /**
     * 인기 상품 TOP 10 (조회수 + 카트수 + 구매수)
     */
    public List<Map<String, Object>> getPopularProducts(LocalDateTime from, LocalDateTime to, int limit) {
        // 상품별 조회수 (PageView 기준)
        List<Object[]> hitCounts = pageViewRepository.countStockViewsGrouped(from, to);
        // 상품별 카트 수
        Map<Long, Long> cartMap = new HashMap<>();
        cartRepository.countByStockGrouped().forEach(row -> cartMap.put((Long) row[0], (Long) row[1]));

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 0;
        for (Object[] row : hitCounts) {
            if (rank >= limit) break;
            Long stId = (Long) row[0];
            long viewCount = (Long) row[1];
            Stock stock = stockRepository.findById(stId).orElse(null);
            if (stock == null) continue;

            long cartCount = cartMap.getOrDefault(stId, 0L);
            long purchaseCount = stock.getStPurCnt() != null ? stock.getStPurCnt() : 0;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", ++rank);
            item.put("stId", stId);
            item.put("stNm", stock.getStNm());
            item.put("views", viewCount);
            item.put("carts", cartCount);
            item.put("purchases", purchaseCount);
            item.put("viewToCart", viewCount > 0 ? Math.round((double) cartCount / viewCount * 1000) / 10.0 : 0);
            item.put("cartToPurchase", cartCount > 0 ? Math.round((double) purchaseCount / cartCount * 1000) / 10.0 : 0);
            item.put("totalConversion", viewCount > 0 ? Math.round((double) purchaseCount / viewCount * 1000) / 10.0 : 0);
            result.add(item);
        }
        return result;
    }

    /**
     * 상품별 전환 우수 / 이탈 TOP 3
     */
    public Map<String, List<Map<String, Object>>> getConversionRanking(LocalDateTime from, LocalDateTime to) {
        List<Map<String, Object>> all = getPopularProducts(from, to, 50);

        // 전환율 기준 정렬 (조회 10건 이상만)
        List<Map<String, Object>> filtered = all.stream()
                .filter(m -> ((Number) m.get("views")).longValue() >= 10)
                .toList();

        List<Map<String, Object>> best = filtered.stream()
                .sorted((a, b) -> Double.compare((Double) b.get("totalConversion"), (Double) a.get("totalConversion")))
                .limit(3).toList();

        List<Map<String, Object>> worst = filtered.stream()
                .sorted((a, b) -> Double.compare((Double) a.get("totalConversion"), (Double) b.get("totalConversion")))
                .limit(3).toList();

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        result.put("best", best);
        result.put("worst", worst);
        return result;
    }

    /**
     * 견적 처리 현황
     */
    public Map<String, Long> getQuoteStatusCounts(LocalDateTime from, LocalDateTime to) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("approved", quoteBaseRepository.countByQuSttAndQuCreDtBetween(QuoteStatus.APPROVED, from, to)
                + quoteBaseRepository.countByQuSttAndQuCreDtBetween(QuoteStatus.PAID, from, to));
        counts.put("submitted", quoteBaseRepository.countByQuSttAndQuCreDtBetween(QuoteStatus.SUBMITTED, from, to));
        counts.put("rejected", quoteBaseRepository.countByQuSttAndQuCreDtBetween(QuoteStatus.REJECTED, from, to));
        counts.put("expired", quoteBaseRepository.countByQuSttAndQuCreDtBetween(QuoteStatus.EXPIRED, from, to));
        return counts;
    }

    /**
     * 월별 가입자 추이 (최근 6개월)
     */
    public List<Map<String, Object>> getMonthlySignups(int year, int month) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.of(year, month).minusMonths(i);
            LocalDateTime from = ym.atDay(1).atStartOfDay();
            LocalDateTime to = ym.atEndOfMonth().atTime(23, 59, 59);
            long count = memberRepository.countByMemCreDtBetween(from, to);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("label", ym.getMonthValue() + "월");
            row.put("count", count);
            result.add(row);
        }
        return result;
    }

    /**
     * 요일별 견적 요청 (이번 달)
     */
    public List<Map<String, Object>> getQuotesByDayOfWeek(LocalDateTime from, LocalDateTime to) {
        String[] days = {"월", "화", "수", "목", "금", "토", "일"};
        long[] counts = new long[7];

        var quotes = quoteBaseRepository.findAllByQuCreDtBetweenOrderByQuCreDtDesc(from, to);
        for (var qb : quotes) {
            if (qb.getQuStt() == null || qb.getQuStt() == QuoteStatus.TEMP_SAVE) continue;
            int dow = qb.getQuCreDt().getDayOfWeek().getValue() - 1; // 0=월 ~ 6=일
            counts[dow]++;
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("label", days[i]);
            row.put("count", counts[i]);
            row.put("isWeekend", i >= 5);
            result.add(row);
        }
        return result;
    }

    /**
     * 시간대별 접속 분포 (0~23시)
     */
    public List<Map<String, Object>> getHourlyTraffic(LocalDateTime from, LocalDateTime to) {
        long[] counts = new long[24];
        List<Object[]> rows = pageViewRepository.countByHourGrouped(from, to);
        for (Object[] row : rows) {
            int hour = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            if (hour >= 0 && hour < 24) counts[hour] = count;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hour", i);
            item.put("count", counts[i]);
            result.add(item);
        }
        return result;
    }

    /**
     * 페이지별 평균 체류 시간
     */
    public List<Map<String, Object>> getPageDwellTime(LocalDateTime from, LocalDateTime to) {
        Map<String, String> pageNames = Map.of(
            "STOCK_DETAIL", "상품 상세",
            "STOCK_LIST", "상품 목록",
            "CART", "장바구니",
            "QUOTE_WRITE", "견적 작성",
            "QUOTE_DETAIL", "견적 상세",
            "PAYMENT_CHECK", "결제 페이지",
            "PAYMENT_RECEIPT", "결제 완료",
            "MYPAGE", "마이페이지",
            "MAIN", "메인페이지"
        );

        List<Object[]> rows = pageViewRepository.avgDwellByPage(from, to);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            String page = (String) row[0];
            double avgSec = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
            long count = ((Number) row[2]).longValue();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("page", page);
            item.put("pageName", pageNames.getOrDefault(page, page));
            item.put("avgSeconds", (int) Math.round(avgSec));
            item.put("avgMinutes", (int)(avgSec / 60));
            item.put("avgRemainSec", (int)(avgSec % 60));
            item.put("views", count);
            result.add(item);
        }
        return result;
    }

    /**
     * 이번 달 신규 가입자 수
     */
    public long getNewSignups(LocalDateTime from, LocalDateTime to) {
        return memberRepository.countByMemCreDtBetween(from, to);
    }

    /**
     * 문의 유형별 통계 (채팅방 제목 기준)
     */
    public List<Map<String, Object>> getInquiryStats(LocalDateTime from, LocalDateTime to) {
        List<ChatRoom> rooms = chatRoomRepository.findAll().stream()
                .filter(r -> r.getChRoCreDt() != null && !r.getChRoCreDt().isBefore(from) && !r.getChRoCreDt().isAfter(to))
                .toList();

        // 제목(=토픽명)별 집계
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ChatRoom r : rooms) {
            String topic = r.getChRoTtl() != null ? r.getChRoTtl().trim() : "기타";
            counts.merge(topic, 1L, Long::sum);
        }

        long total = rooms.size();
        List<Map<String, Object>> result = new ArrayList<>();
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("category", e.getKey());
                    item.put("count", e.getValue());
                    item.put("pct", total > 0 ? Math.round(e.getValue() * 100.0 / total) : 0);
                    result.add(item);
                });

        return result;
    }

    /**
     * 평균 세션 시간 (초)
     */
    public Map<String, Object> getSessionStats(LocalDateTime from, LocalDateTime to) {
        Map<String, Object> result = new LinkedHashMap<>();
        Double avg = sessionLogRepository.avgDurationBetween(from, to);
        result.put("avgSeconds", avg != null ? Math.round(avg) : 0);
        result.put("avgMinutes", avg != null ? (int)(avg / 60) : 0);
        result.put("avgRemainSeconds", avg != null ? (int)(avg % 60) : 0);

        // 구간별 분포
        Object[] dist = sessionLogRepository.durationDistribution(from, to);
        long[] counts = new long[5];
        if (dist != null && dist.length > 0) {
            // 단일 행 반환
            Object[] row = dist;
            if (dist[0] instanceof Object[]) row = (Object[]) dist[0];
            for (int i = 0; i < 5 && i < row.length; i++) {
                counts[i] = row[i] != null ? ((Number) row[i]).longValue() : 0;
            }
        }
        long total = 0;
        for (long c : counts) total += c;

        String[] labels = {"1분 미만", "1 ~ 5분", "5 ~ 15분", "15 ~ 30분", "30분 이상"};
        List<Map<String, Object>> distribution = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", labels[i]);
            item.put("count", counts[i]);
            item.put("pct", total > 0 ? Math.round(counts[i] * 100.0 / total) : 0);
            distribution.add(item);
        }
        result.put("distribution", distribution);
        return result;
    }
}
