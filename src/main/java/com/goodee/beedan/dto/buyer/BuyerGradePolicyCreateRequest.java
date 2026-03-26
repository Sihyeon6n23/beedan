package com.goodee.beedan.dto.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerGradePolicyCreateRequest {

    @NotBlank(message = "등급은 필수입니다")
    private String bgpGr; // STANDARD PREMIUM VIP
    private Integer bgpMinOrdCnt;   // 최소 거래 횟수(null=미적용)
    private BigDecimal bgpMinTtAm;  // 최소 누적 금액
    @NotNull(message = "정책 시작일은 필수입니다")
    private LocalDateTime bgpEfFrDt;    // 정책 시작일
    private LocalDateTime bgpEfToDt;    // 정책 종료일 (null = 무기한)
    private String bgpDes;
}
