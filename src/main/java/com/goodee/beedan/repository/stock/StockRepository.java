package com.goodee.beedan.repository.stock;

import com.goodee.beedan.config.exception.EntityNotFoundException;
import com.goodee.beedan.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long>, JpaSpecificationExecutor<Stock> {
    Page<Stock> findByStExpYnTrue(Pageable pageable);
    long countByBrId(Long brId);
    java.util.Optional<Stock> findByBrIdAndStNm(Long brId, String stNm);

    Page<Stock> findByStIdInAndStExpYnTrue(List<Long> stIds, Pageable pageable);

    List<Stock> findTop30ByStExpYnTrueOrderByStCraDtDesc();

    @Query("SELECT DISTINCT s.brId FROM Stock s WHERE s.stReqYn = false")
    List<Long> findDistinctBrandIdsWithStock();

    @Query("SELECT s FROM Stock s " +
            "JOIN HitStock h ON s.stId = h.stId " +
            "WHERE h.hitDt >= :start AND h.hitDt < :end " +
            "GROUP BY s.stId " +
            "ORDER BY COUNT(h.hitId) DESC")
    List<Stock> findPopularStocksByPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    List<Stock> findByBrId(Long brId);

    @Query("SELECT s.stCd FROM Stock s WHERE s.brId = :brId ORDER BY s.stCd DESC LIMIT 1")
    java.util.Optional<String> findTopStCdByBrIdOrderByStCdDesc(@Param("brId") Long brId);

    default Stock getByIdOrThrow(Long stId) {
        return findById(stId).orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + stId));
    }
}
