package com.goodee.beedan.dto.quote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 견적 배송비 상세 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteShipFeeRequest {

    @NotNull(message = "견적 상세 아이디는 필수입니다")
    private Long quInfoId;

    @NotNull(message = "견적 아이디는 필수입니다")
    private Long quId;

    @NotNull(message = "협상 아이디는 필수입니다")
    private Long ngId;

    @NotNull(message = "공장 아이디는 필수입니다")
    private Long faId;

    private String qsfFaNm;             // 공장명

    private String qsfFaCCd;            // 공장 국가코드

    @NotBlank(message = "운송 수단은 필수입니다")
    private String qsfTrspTy;           // SEA | AIR | EXPRESS

    @NotNull(message = "총 다스 수량은 필수입니다")
    private Integer qsfTtDz;            // 총 다스 수량
}