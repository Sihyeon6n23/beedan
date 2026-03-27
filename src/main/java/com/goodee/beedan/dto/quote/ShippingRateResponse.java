package com.goodee.beedan.dto.quote;

import com.goodee.beedan.common.constant.TransportType;
import com.goodee.beedan.entity.ShippingRate;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ShippingRateResponse {

    private Long srId;
    private String srCCd;
    private TransportType srTrspTy;
    private Integer srSmQn;
    private BigDecimal srSmAm;
    private Integer srMdQn;
    private BigDecimal srMdAm;
    private Integer srLgQn;
    private BigDecimal srLgAm;
    private Boolean srYn;
    private String srDes;
    private LocalDateTime srCrDt;
    private LocalDateTime srUpDt;

    public static ShippingRateResponse from(ShippingRate shippingRate) {
        return ShippingRateResponse.builder()
                .srId(shippingRate.getSrId())
                .srCCd(shippingRate.getSrCCd())
                .srTrspTy(shippingRate.getSrTrspTy())
                .srSmQn(shippingRate.getSrSmQn())
                .srSmAm(shippingRate.getSrSmAm())
                .srMdQn(shippingRate.getSrMdQn())
                .srMdAm(shippingRate.getSrMdAm())
                .srLgQn(shippingRate.getSrLgQn())
                .srLgAm(shippingRate.getSrLgAm())
                .srYn(shippingRate.getSrYn())
                .srDes(shippingRate.getSrDes())
                .srCrDt(shippingRate.getSrCrDt())
                .srUpDt(shippingRate.getSrUpDt())
                .build();
    }
}