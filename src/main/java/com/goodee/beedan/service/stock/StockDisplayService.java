package com.goodee.beedan.service.stock;

import com.goodee.beedan.dto.stock.StockListDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDisplayService {

    private static final String KEY_NEW_STOCKS = "display:newStocks";
    private static final String KEY_POPULAR_STOCKS = "display:popularStocks";

    private final StockService stockService;
    private final RedisTemplate<String, Object> redisTemplate;

    // redis에 데이터 없으면 최초 삽입
    @PostConstruct
    public void init() {
        if(!redisTemplate.hasKey(KEY_NEW_STOCKS)) {
            log.info("캐시에 신규 상품 데이터 x");
            refreshNewStocks();
        }
        if(!redisTemplate.hasKey(KEY_POPULAR_STOCKS)) {
            log.info("캐시에 인기 상품 데이터 x");
            refreshPopularStocks();
        }
        log.info("캐시에 데이터 존재");
    }

    public void refreshNewStocks() {
        try {
            List<StockListDto> result = stockService.findNewStocks();
            redisTemplate.opsForValue().set(KEY_NEW_STOCKS, result);
            log.info("신규 상품 전시 캐시 갱신 완료 ({}건)", result.size());
        } catch (Exception e) {
            log.error("신규 상품 전시 캐시 갱신 실패", e);
        }
    }

    public void refreshPopularStocks() {
        try {
            List<StockListDto> result = stockService.findPopularStocks();
            redisTemplate.opsForValue().set(KEY_POPULAR_STOCKS, result);
            log.info("전월 인기 상품 캐시 갱신 완료 ({}건)", result.size());
        } catch (Exception e) {
            log.error("전월 인기 상품 캐시 갱신 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<StockListDto> getNewStocks() {
        Object cached = redisTemplate.opsForValue().get(KEY_NEW_STOCKS);
        if (cached instanceof List) {
            log.info("신규 상품 전시 캐시에서 데이터 조회 ({}건)", ((List<?>) cached).size());
            return (List<StockListDto>) cached;
        }
        log.info("신규 상품 전시 캐시에 데이터 없음, DB에서 조회");
        return stockService.findNewStocks();
    }

    @SuppressWarnings("unchecked")
    public List<StockListDto> getPopularStocks() {
        Object cached = redisTemplate.opsForValue().get(KEY_POPULAR_STOCKS);
        if (cached instanceof List) {
            log.info("전월 인기 상품 캐시에서 데이터 조회 ({}건)", ((List<?>) cached).size());
            return (List<StockListDto>) cached;
        }
        log.info("전월 인기 상품 캐시에 데이터 없음, DB에서 조회");
        return stockService.findPopularStocks();
    }
}
