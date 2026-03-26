package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.UnitDiscount;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 묶음별 할인 정책 응답 DTO
 */
@Getter
@Builder
public class UnitDiscountResponse {

    private Long unDId;
    private Long unGId;
    private Integer unDMinQn;
    private BigDecimal unDMinAm;
    private BigDecimal unDQnDr;
    private BigDecimal unDAmDr;
    private String unDOvTy;
    private String unDDes;
    private LocalDateTime unDCrDt;
    private LocalDateTime unDUpDt;

    public static UnitDiscountResponse from(UnitDiscount unitDiscount) {
        return UnitDiscountResponse.builder()
                .unDId(unitDiscount.getUnDId())
                .unGId(unitDiscount.getUnGId())
                .unDMinQn(unitDiscount.getUnDMinQn())
                .unDMinAm(unitDiscount.getUnDMinAm())
                .unDQnDr(unitDiscount.getUnDQnDr())
                .unDAmDr(unitDiscount.getUnDAmDr())
                .unDOvTy(unitDiscount.getUnDOvTy().name())
                .unDDes(unitDiscount.getUnDDes())
                .unDCrDt(unitDiscount.getUnDCrDt())
                .unDUpDt(unitDiscount.getUnDUpDt())
                .build();
    }
}