package com.goodee.beedan.repository.hitstock;

import com.goodee.beedan.entity.HitStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HitStockRepository extends JpaRepository<HitStock, Long> {
}
