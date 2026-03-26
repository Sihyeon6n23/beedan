package com.goodee.beedan.repository.buyer;

import com.goodee.beedan.entity.FeePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeePolicyRepository extends JpaRepository<FeePolicy, Long> {
    Optional<FeePolicy> findByBgpGrAndFpFeeTyAndFpAcYnTrue(String bgpGr, String fpFeeTy);

    List<FeePolicy> findAllByBgpGrAndFpAcYnTrue(String bgpGr);

    List<FeePolicy> findAllByFpAcYnTrue();
}
