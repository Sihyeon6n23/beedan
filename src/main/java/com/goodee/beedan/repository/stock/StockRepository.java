package com.goodee.beedan.repository.stock;

import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.entity.Stock;
import io.micrometer.observation.ObservationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long>, JpaSpecificationExecutor<Stock> {
    Page<Stock> findByStExpYnTrue(Pageable pageable);
    long countByBrId(Long brId);
    java.util.Optional<Stock> findByBrIdAndStNm(Long brId, String stNm);

    Page<Stock> findByStIdInAndStExpYnTrue(List<Long> stIds, Pageable pageable);

    List<Stock> findTop30ByStExpYnTrueOrderByStCraDtDesc();
}
