package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.QuoteDetail;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 견적 품목 상세 응답 DTO
 */
@Getter
@Builder
public class QuoteDetailResponse {

    private Long quDtId;
    private Long quInfoId;
    private Long quId;
    private Long ngId;
    private Long stId;
    private String stNm;
    private Integer quDtQn;
    private Long faId;
    private String faNm;
    private Long unGId;
    private String unGNm;
    private Integer quUQn;
    private BigDecimal quDtFgPr;
    private BigDecimal quDtKrPr;
    private BigDecimal quDtPr;

    public static QuoteDetailResponse from(QuoteDetail quoteDetail) {
        return QuoteDetailResponse.builder()
                .quDtId(quoteDetail.getQuDtId())
                .quInfoId(quoteDetail.getQuInfoId())
                .quId(quoteDetail.getQuId())
                .ngId(quoteDetail.getNgId())
                .stId(quoteDetail.getStId())
                .stNm(quoteDetail.getStNm())
                .quDtQn(quoteDetail.getQuDtQn())
                .faId(quoteDetail.getFaId())
                .faNm(quoteDetail.getFaNm())
                .unGId(quoteDetail.getUnGId())
                .unGNm(quoteDetail.getUnGNm())
                .quUQn(quoteDetail.getQuUQn())
                .quDtFgPr(quoteDetail.getQuDtFgPr())
                .quDtKrPr(quoteDetail.getQuDtKrPr())
                .quDtPr(quoteDetail.getQuDtPr())
                .build();
    }
}