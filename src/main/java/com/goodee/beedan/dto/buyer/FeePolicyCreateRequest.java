package com.goodee.beedan.dto.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 비용 정책 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class FeePolicyCreateRequest {

    @NotBlank(message = "등급은 필수입니다")
    private String bgpGr;               // STANDARD | PREMIUM | VIP

    @NotBlank(message = "비용 항목은 필수입니다")
    private String fpFeeTy;             // SERVICE_COMMISSION | SHIPPING | CUSTOMS

    @NotBlank(message = "계산 방식은 필수입니다")
    private String fpCalcTy;            // RATE | FIXED

    @NotNull(message = "계산 값은 필수입니다")
    private BigDecimal fpVal;

    @NotNull(message = "적용 시작일은 필수입니다")
    private LocalDateTime fpEfFrDt;

    private LocalDateTime fpEfToDt;     // null = 무기한

    private String fpDes;
}