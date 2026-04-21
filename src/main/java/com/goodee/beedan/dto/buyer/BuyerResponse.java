package com.goodee.beedan.dto.buyer;

import com.goodee.beedan.entity.Buyer;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class BuyerResponse {
    private Long byId;
    private String memBizNo;
    private String memBizTtl;
    private String bgpGr;
    private Integer byOrdCnt;
    private BigDecimal byTtlAm;
    private LocalDateTime byFrDt;
    private LocalDateTime byLtDt;

    public static BuyerResponse from(Buyer buyer) {
        return BuyerResponse.builder()
                .byId(buyer.getById())
                .memBizNo(buyer.getMemBizNo())
                .memBizTtl(buyer.getMemBizTtl())
                .bgpGr(buyer.getBgpGr())
                .byOrdCnt(buyer.getByOrdCnt())
                .byTtlAm(buyer.getByTtlAm())
                .byFrDt(buyer.getByFrDt())
                .byLtDt(buyer.getByLtDt())
                .build();
    }
}
