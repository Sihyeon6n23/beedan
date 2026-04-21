package com.goodee.beedan.dto.buyer;

import com.goodee.beedan.entity.BuyerGradePolicy;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@Builder
public class BuyerGradePolicyResponse {

    private Long bgpId;
    private String bgpGr;
    private Integer bgpMinOrdCnt;
    private BigDecimal bgpMinTtAm;
    private LocalDateTime bgpEfFrDt;
    private LocalDateTime bgpEfToDt;
    private List<String> bgpDes;
    private Boolean bgpAcYn;
    private LocalDateTime bgpCrDt;
    private LocalDateTime bgpUpDt;

    public static BuyerGradePolicyResponse from(BuyerGradePolicy policy){
        return BuyerGradePolicyResponse.builder()
                .bgpId(policy.getBgpId())
                .bgpGr(policy.getBgpGr())
                .bgpMinOrdCnt(policy.getBgpMinOrdCnt())
                .bgpMinTtAm(policy.getBgpMinTtAm())
                .bgpEfFrDt(policy.getBgpEfFrDt())
                .bgpEfToDt(policy.getBgpEfToDt())
                .bgpDes(policy.getBgpDes() != null ?
                        Arrays.asList(policy.getBgpDes().split(",")) : null)
                .bgpAcYn(policy.getBgpAcYn())
                .bgpCrDt(policy.getBgpCrDt())
                .bgpUpDt(policy.getBgpUpDt())
                .build();
    }
}
