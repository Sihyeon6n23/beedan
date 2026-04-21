package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.QuoteShipFee;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 견적 배송비 상세 응답 DTO
 */
@Getter
@Builder
public class QuoteShipFeeResponse {

    private Long qsfId;
    private Long quInfoId;
    private Long quId;
    private Long ngId;
    private Long faId;
    private String qsfFaNm;
    private String qsfFaCCd;
    private String qsfTrspTy;
    private Integer qsfTtDz;
    private BigDecimal qsfSrAm;
    private Boolean qsfSrYn;
    private String qsfSrDes;
    private BigDecimal qsfPrtAm;
    private BigDecimal qsfCstAm;
    private BigDecimal qsfHsCd;
    private Boolean qsfInsYn;
    private BigDecimal qsfInsAm;
    private BigDecimal qsfCifAm;
    private BigDecimal qsfDtyR;
    private BigDecimal qsfDty;
    private BigDecimal qsfVat;
    private BigDecimal qsfDscR;
    private BigDecimal qsfDscAm;
    private BigDecimal qsfTtl;

    public static QuoteShipFeeResponse from(QuoteShipFee quoteShipFee) {
        return QuoteShipFeeResponse.builder()
                .qsfId(quoteShipFee.getQsfId())
                .quInfoId(quoteShipFee.getQuInfoId())
                .quId(quoteShipFee.getQuId())
                .ngId(quoteShipFee.getNgId())
                .faId(quoteShipFee.getFaId())
                .qsfFaNm(quoteShipFee.getQsfFaNm())
                .qsfFaCCd(quoteShipFee.getQsfFaCCd())
                .qsfTrspTy(quoteShipFee.getQsfTrspTy())
                .qsfTtDz(quoteShipFee.getQsfUnQn())
                .qsfSrAm(quoteShipFee.getQsfSrAm())
                .qsfSrYn(quoteShipFee.getQsfSrYn())
                .qsfSrDes(quoteShipFee.getQsfSrDes())
                .qsfPrtAm(quoteShipFee.getQsfPrtAm())
                .qsfCstAm(quoteShipFee.getQsfCstAm())
                .qsfHsCd(quoteShipFee.getQsfHsCd())
                .qsfInsYn(quoteShipFee.getQsfInsYn())
                .qsfInsAm(quoteShipFee.getQsfInsAm())
                .qsfCifAm(quoteShipFee.getQsfCifAm())
                .qsfDtyR(quoteShipFee.getQsfDtyR())
                .qsfDty(quoteShipFee.getQsfDty())
                .qsfVat(quoteShipFee.getQsfVat())
                .qsfDscR(quoteShipFee.getQsfDscR())
                .qsfDscAm(quoteShipFee.getQsfDscAm())
                .qsfTtl(quoteShipFee.getQsfTtl())
                .build();
    }
}