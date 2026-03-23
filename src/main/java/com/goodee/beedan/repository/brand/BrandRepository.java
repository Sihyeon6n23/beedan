package com.goodee.beedan.repository.brand;

import com.goodee.beedan.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByBrNm(String brNm);
}
