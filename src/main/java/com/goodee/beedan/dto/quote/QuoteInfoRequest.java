package com.goodee.beedan.dto.quote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 견적 상세 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteInfoRequest {

    @NotNull(message = "견적 아이디는 필수입니다")
    private Long quId;

    @NotNull(message = "협상 아이디는 필수입니다")
    private Long ngId;

    @NotBlank(message = "통화 코드는 필수입니다")
    private String quInfoCurCd;         // JPY|USD|EUR|CNY

    @NotNull(message = "환율은 필수입니다")
    private BigDecimal quInfoExcRt;     // 적용 환율

    @NotNull(message = "적용 등급 아이디는 필수입니다")
    private Long bgpId;                 // BUYER_GRADE_POLICY 아이디

    @NotNull(message = "적용 비용 정책 아이디는 필수입니다")
    private Long fpId;                  // FEE_POLICY 아이디

    private LocalDateTime quInfoDsrDt;  // 희망 수령일
    private String quInfoPs;            // 특기사항
}