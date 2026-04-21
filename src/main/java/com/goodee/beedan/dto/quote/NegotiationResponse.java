package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.Negotiation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NegotiationResponse {

    private Long ngId;
    private String ngNm;
    private Long memId;
    private LocalDateTime ngCreDt;
    private LocalDateTime ngEndDt;
    private boolean isOngoing;

    public static NegotiationResponse from(Negotiation negotiation) {
        return NegotiationResponse.builder()
                .ngId(negotiation.getNgId())
                .ngNm(negotiation.getNgNm())
                .memId(negotiation.getMemId())
                .ngCreDt(negotiation.getNgCreDt())
                .ngEndDt(negotiation.getNgEndDt())
                .isOngoing(negotiation.isOngoing())
                .build();
    }
}