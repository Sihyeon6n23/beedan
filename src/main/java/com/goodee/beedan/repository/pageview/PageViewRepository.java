package com.goodee.beedan.repository.pageview;

import com.goodee.beedan.entity.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PageViewRepository extends JpaRepository<PageView, Long> {

    // 기간 내 특정 페이지 조회수
    long countByPvPageAndPvCreDtBetween(String pvPage, LocalDateTime from, LocalDateTime to);

    // 기간 내 전체 조회수
    long countByPvCreDtBetween(LocalDateTime from, LocalDateTime to);

    // 기간 내 상품별 조회수 (상품 상세 페이지)
    @Query("SELECT pv.pvRefId, COUNT(pv) FROM PageView pv WHERE pv.pvPage = 'STOCK_DETAIL' AND pv.pvCreDt BETWEEN :from AND :to GROUP BY pv.pvRefId ORDER BY COUNT(pv) DESC")
    List<Object[]> countStockViewsGrouped(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 기간 내 시간대별 조회수
    @Query("SELECT HOUR(pv.pvCreDt), COUNT(pv) FROM PageView pv WHERE pv.pvCreDt BETWEEN :from AND :to GROUP BY HOUR(pv.pvCreDt) ORDER BY HOUR(pv.pvCreDt)")
    List<Object[]> countByHourGrouped(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 페이지별 평균 체류 시간 + 조회수
    @Query("SELECT pv.pvPage, AVG(pv.pvDwellSec), COUNT(pv) FROM PageView pv WHERE pv.pvDwellSec IS NOT NULL AND pv.pvCreDt BETWEEN :from AND :to GROUP BY pv.pvPage ORDER BY AVG(pv.pvDwellSec) DESC")
    List<Object[]> avgDwellByPage(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 상품별 조회수 + 상품명 JOIN (N+1 제거)
    @Query("SELECT pv.pvRefId, COUNT(pv), s.stNm, s.stPurCnt FROM PageView pv JOIN Stock s ON pv.pvRefId = s.stId WHERE pv.pvPage = 'STOCK_DETAIL' AND pv.pvCreDt BETWEEN :from AND :to GROUP BY pv.pvRefId, s.stNm, s.stPurCnt ORDER BY COUNT(pv) DESC")
    List<Object[]> countStockViewsWithName(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 기간 내 상품별 장바구니 담기 이벤트 수 (ADD_TO_CART)
    @Query("SELECT pv.pvRefId, COUNT(pv) FROM PageView pv WHERE pv.pvPage = 'ADD_TO_CART' AND pv.pvCreDt BETWEEN :from AND :to GROUP BY pv.pvRefId")
    List<Object[]> countCartAddsGrouped(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
