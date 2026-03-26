package com.goodee.beedan.dto.buyer;

import com.goodee.beedan.entity.FeePolicy;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 비용 정책 응답 DTO
 */
@Getter
@Builder
public class FeePolicyResponse {

    private Long fpId;
    private String bgpGr;
    private String fpFeeTy;
    private String fpCalcTy;
    private BigDecimal fpVal;
    private Boolean fpAcYn;
    private LocalDateTime fpEfFrDt;
    private LocalDateTime fpEfToDt;
    private String fpDes;
    private LocalDateTime fpCrDt;
    private LocalDateTime fpUpDt;

    public static FeePolicyResponse from(FeePolicy policy) {
        return FeePolicyResponse.builder()
                .fpId(policy.getFpId())
                .bgpGr(policy.getBgpGr())
                .fpFeeTy(policy.getFpFeeTy())
                .fpCalcTy(policy.getFpCalcTy().name())
                .fpVal(policy.getFpVal())
                .fpAcYn(policy.getFpAcYn())
                .fpEfFrDt(policy.getFpEfFrDt())
                .fpEfToDt(policy.getFpEfToDt())
                .fpDes(policy.getFpDes())
                .fpCrDt(policy.getFpCrDt())
                .fpUpDt(policy.getFpUpDt())
                .build();
    }
}