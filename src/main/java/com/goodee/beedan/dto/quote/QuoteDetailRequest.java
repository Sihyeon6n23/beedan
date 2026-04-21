package com.goodee.beedan.dto.quote;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 견적 품목 상세 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteDetailRequest {

    @NotNull(message = "견적 상세 아이디는 필수입니다")
    private Long quInfoId;

    @NotNull(message = "견적 아이디는 필수입니다")
    private Long quId;

    @NotNull(message = "협상 아이디는 필수입니다")
    private Long ngId;

    private Long stId;                      // 상품 아이디

    @NotBlank(message = "상품명은 필수입니다")
    private String stNm;                    // 상품명

    @NotNull(message = "상품 수량은 필수입니다")
    @Min(value = 1, message = "상품 수량은 1 이상이어야 합니다")
    private Integer quDtQn;                 // 상품 수량

    private Long faId;                      // 공장 아이디

    private String faNm;                    // 공장명

    private Long unGId;                     // 묶음 단위 아이디

    private String unGNm;                   // 묶음 단위명

    private Integer quUQn;                  // 묶음 수량

    @NotNull(message = "외화 단가는 필수입니다")
    private BigDecimal quDtFgPr;            // 외화 단가
}