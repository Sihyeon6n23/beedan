package com.goodee.beedan.service.stock;

import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.repository.stock.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDisplayService {

    private final StockRepository stockRepository;
    private final StockService stockService;

    private volatile List<StockListDto> cachedNewStocks;
    private volatile List<StockListDto> cachedPopularStocks;

    @PostConstruct
    public void init() {
        refreshNewStocks();
        refreshPopularStocks();
    }

    public void refreshNewStocks() {
        try {
            List<Stock> newStocks = stockRepository.findTop30ByStExpYnTrueOrderByStCraDtDesc();
            Collections.shuffle(newStocks);
            cachedNewStocks = newStocks.stream()
                    .limit(16)
                    .map(stock -> stockService.mapToStockListDto(stock, Collections.emptySet()))
                    .collect(Collectors.toList());
            log.info("신규 상품 전시 캐시 갱신 완료 ({}건)", cachedNewStocks.size());
        } catch (Exception e) {
            log.error("신규 상품 전시 캐시 갱신 실패", e);
        }
    }

    public void refreshPopularStocks() {
        try {
            // 1. 전월(Last Month) 범위 계산
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            LocalDateTime startDt = lastMonth.atDay(1).atStartOfDay(); // 예: 2026-03-01 00:00:00
            LocalDateTime endDt = lastMonth.plusMonths(1).atDay(1).atStartOfDay(); // 예: 2026-04-01 00:00:00

            // 2. DB에서 상위 16개 추출 (PageRequest 사용)
            List<Stock> popularStocks = stockRepository.findPopularStocksByPeriod(
                    startDt, endDt, PageRequest.of(0, 16)
            );

            // 3. 캐시 업데이트
            cachedPopularStocks = popularStocks.stream()
                    .map(stock -> stockService.mapToStockListDto(stock, Collections.emptySet()))
                    .collect(Collectors.toList());

            log.info("전월 인기 상품 캐시 갱신 완료 ({}년 {}월 기준, {}건)",
                    lastMonth.getYear(), lastMonth.getMonthValue(), cachedPopularStocks.size());

        } catch (Exception e) {
            log.error("전월 인기 상품 캐시 갱신 실패", e);
        }
    }

    public List<StockListDto> getNewStocks() {
        if (cachedNewStocks != null) {
            return cachedNewStocks;
        }
        return stockService.findNewStocks();
    }

    public List<StockListDto> getPopularStocks() {
        if (cachedPopularStocks != null) {
            return cachedPopularStocks;
        }
        return Collections.emptyList();
    }
}
