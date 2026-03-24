package com.goodee.beedan.repository.stock;

import com.goodee.beedan.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockRepository extends JpaRepository<Stock, Long>, JpaSpecificationExecutor<Stock> {
    Page<Stock> findByStExpYnTrue(Pageable pageable);
    long countByBrId(Long brId);
    java.util.Optional<Stock> findByBrIdAndStNm(Long brId, String stNm);
}
