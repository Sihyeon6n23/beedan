package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.StockInspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockInspectionRepository extends JpaRepository<StockInspection, Long> {
    List<StockInspection> findAllByStiYnTrue();
}
