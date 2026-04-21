package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.PortCustomsRate;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 항만비/통관수수료 응답 DTO
 */
@Getter
@Builder
public class PortCustomsRateResponse {

    private Long pcrId;
    private String pcrTy;
    private BigDecimal pcrSmAm;
    private BigDecimal pcrMdAm;
    private BigDecimal pcrLgAm;
    private Boolean pcrYn;
    private String pcrDes;
    private LocalDateTime pcrCrDt;
    private LocalDateTime pcrUpDt;

    public static PortCustomsRateResponse from(PortCustomsRate portCustomsRate) {
        return PortCustomsRateResponse.builder()
                .pcrId(portCustomsRate.getPcrId())
                .pcrTy(portCustomsRate.getPcrTy())
                .pcrSmAm(portCustomsRate.getPcrSmAm())
                .pcrMdAm(portCustomsRate.getPcrMdAm())
                .pcrLgAm(portCustomsRate.getPcrLgAm())
                .pcrYn(portCustomsRate.getPcrYn())
                .pcrDes(portCustomsRate.getPcrDes())
                .pcrCrDt(portCustomsRate.getPcrCrDt())
                .pcrUpDt(portCustomsRate.getPcrUpDt())
                .build();
    }
}