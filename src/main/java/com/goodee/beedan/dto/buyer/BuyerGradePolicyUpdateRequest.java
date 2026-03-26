package com.goodee.beedan.dto.buyer;

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
public class BuyerGradePolicyUpdateRequest {

    private Integer bgpMinOrdCnt;
    private BigDecimal bgpMinTtAm;
    private LocalDateTime bgpEfFrDt;
    private LocalDateTime bgpEfToDt;
    private String bgpDes;

}
