package com.goodee.beedan.repository.buyer;

import com.goodee.beedan.entity.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuyerRepository extends JpaRepository<Buyer, Long> {

    Optional<Buyer> findByMemBizNo(String memBizNo);

    boolean existsByMemBizNo(String memBizNo);

    // 등급별 조회
    List<Buyer> findAllByBgpGr(String bgpGr);

}
