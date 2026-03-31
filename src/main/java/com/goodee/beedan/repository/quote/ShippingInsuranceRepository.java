package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.ShippingInsurance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShippingInsuranceRepository extends JpaRepository<ShippingInsurance, Long> {
    List<ShippingInsurance> findAllBySiYnTrue();
}
