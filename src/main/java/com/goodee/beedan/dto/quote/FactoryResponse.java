package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.Factory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FactoryResponse {

    private Long faId;
    private Long brId;
    private String faNm;
    private String faAd;
    private String faCty;
    private String faCCd;
    private Boolean faYn;
    private LocalDateTime faCrDt;
    private LocalDateTime faUpDt;

    public static FactoryResponse from(Factory factory) {
        return FactoryResponse.builder()
                .faId(factory.getFaId())
                .brId(factory.getBrId())
                .faNm(factory.getFaNm())
                .faAd(factory.getFaAd())
                .faCty(factory.getFaCty())
                .faCCd(factory.getFaCCd())
                .faYn(factory.getFaYn())
                .faCrDt(factory.getFaCrDt())
                .faUpDt(factory.getFaUpDt())
                .build();
    }
}