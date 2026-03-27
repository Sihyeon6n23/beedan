package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.QuoteBase;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 견적 응답 DTO
 */
@Getter
@Builder
public class QuoteBaseResponse {

    private Long quId;
    private Long ngId;
    private Long quSid;
    private Long quRid;
    private String quStt;
    private LocalDateTime quExpDt;
    private Boolean quOpenYn;
    private String quCon;
    private LocalDateTime quCreDt;
    private LocalDateTime quUpdDt;
    private boolean isEditable;
    private boolean isExpired;

    public static QuoteBaseResponse from(QuoteBase quoteBase) {
        return QuoteBaseResponse.builder()
                .quId(quoteBase.getQuId())
                .ngId(quoteBase.getNgId())
                .quSid(quoteBase.getQuSid())
                .quRid(quoteBase.getQuRid())
                .quStt(quoteBase.getQuStt().name())
                .quExpDt(quoteBase.getQuExpDt())
                .quOpenYn(quoteBase.getQuOpYn())
                .quCon(quoteBase.getQuCon())
                .quCreDt(quoteBase.getQuCreDt())
                .quUpdDt(quoteBase.getQuUpdDt())
                .isEditable(quoteBase.isEditable())
                .isExpired(quoteBase.isExpired())
                .build();
    }
}