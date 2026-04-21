package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.DomesticDeliveryRate;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 국내 지역별 배송비 응답 DTO
 */
@Getter
@Builder
public class DomesticDeliveryRateResponse {

    private Long ddrId;
    private String ddrRgn;
    private BigDecimal ddrAm;
    private BigDecimal ddrEAm;
    private BigDecimal totalAmount;
    private String ddrDes;
    private Boolean ddrYn;
    private LocalDateTime ddrCrDt;
    private LocalDateTime ddrUpDt;

    public static DomesticDeliveryRateResponse from(DomesticDeliveryRate rate) {
        return DomesticDeliveryRateResponse.builder()
                .ddrId(rate.getDdrId())
                .ddrRgn(rate.getDdrRgn())
                .ddrAm(rate.getDdrAm())
                .ddrEAm(rate.getDdrEAm())
                .totalAmount(rate.totalAmount())
                .ddrDes(rate.getDdrDes())
                .ddrYn(rate.getDdrYn())
                .ddrCrDt(rate.getDdrCrDt())
                .ddrUpDt(rate.getDdrUpDt())
                .build();
    }
}