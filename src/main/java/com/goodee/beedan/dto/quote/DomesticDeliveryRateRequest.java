package com.goodee.beedan.dto.quote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 국내 지역별 배송비 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomesticDeliveryRateRequest {

    @NotBlank(message = "지역 구분은 필수입니다")
    private String ddrRgn;              // SEOUL|GYEONGGI|METRO|PROVINCE|JEJU|ISLAND

    @NotNull(message = "기본 배송비는 필수입니다")
    private BigDecimal ddrAm;           // 기본 배송비

    private BigDecimal ddrEAm;          // 추가 배송비 (null = 0)

    private String ddrDes;              // 관리자 메모
}