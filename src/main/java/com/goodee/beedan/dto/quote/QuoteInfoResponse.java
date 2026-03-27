package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.QuoteInfo;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 견적 상세 응답 DTO
 */
@Getter
@Builder
public class QuoteInfoResponse {

    private Long quInfoId;
    private Long quId;
    private Long ngId;
    private BigDecimal quInfoExcRt;
    private String quInfoCurCd;
    private Long bgpId;
    private Long fpId;
    private BigDecimal quInfoSrvFe;
    private BigDecimal quInfoSrvFeR;
    private BigDecimal quInfoSrvFeAm;
    private BigDecimal quInfoDdAm;
    private BigDecimal quInfoDdExAm;
    private BigDecimal quInfoIntShiFe;
    private BigDecimal quInfoTtlShiFe;
    private BigDecimal quInfoTax;
    private BigDecimal quInfoTp;
    private BigDecimal quInfoDisTp;
    private String quInfoPs;
    private LocalDateTime quInfoDsrDt;
    private LocalDateTime quInfoCreDt;
    private LocalDateTime quInfoUpdDt;

    public static QuoteInfoResponse from(QuoteInfo quoteInfo) {
        return QuoteInfoResponse.builder()
                .quInfoId(quoteInfo.getQuInfoId())
                .quId(quoteInfo.getQuId())
                .ngId(quoteInfo.getNgId())
                .quInfoExcRt(quoteInfo.getQuInfoExcRt())
                .quInfoCurCd(quoteInfo.getQuInfoCurCd())
                .bgpId(quoteInfo.getBgpId())
                .fpId(quoteInfo.getFpId())
                .quInfoSrvFe(quoteInfo.getQuInfoSrvFe())
                .quInfoSrvFeR(quoteInfo.getQuInfoSrvFeR())
                .quInfoSrvFeAm(quoteInfo.getQuInfoSrvFeAm())
                .quInfoDdAm(quoteInfo.getQuInfoDdAm())
                .quInfoDdExAm(quoteInfo.getQuInfoDdExAm())
                .quInfoIntShiFe(quoteInfo.getQuInfoIntShiFe())
                .quInfoTtlShiFe(quoteInfo.getQuInfoTtlShiFe())
                .quInfoTax(quoteInfo.getQuInfoTax())
                .quInfoTp(quoteInfo.getQuInfoTp())
                .quInfoDisTp(quoteInfo.getQuInfoDisTp())
                .quInfoPs(quoteInfo.getQuInfoPs())
                .quInfoDsrDt(quoteInfo.getQuInfoDsrDt())
                .build();
    }
}