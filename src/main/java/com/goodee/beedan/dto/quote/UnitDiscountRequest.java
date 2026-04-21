package com.goodee.beedan.dto.quote;

import com.goodee.beedan.common.constant.OverlapType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 묶음별 할인 정책 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitDiscountRequest {

    @NotNull(message = "묶음 단위 아이디는 필수입니다")
    private Long unGId;

    private Integer unDMinQn;           // 최소 묶음 수량 (null = 미적용)

    private BigDecimal unDMinAm;        // 최소 금액 (null = 미적용)

    private BigDecimal unDQnDr;         // 수량 할인율

    private BigDecimal unDAmDr;         // 금액 할인율

    @NotNull(message = "중복 충족 처리방식은 필수입니다")
    private OverlapType unDOvTy;        // HIGHER | LOWER | MULTIPLY | FIXED

    private String unDDes;              // 관리자 메모
}