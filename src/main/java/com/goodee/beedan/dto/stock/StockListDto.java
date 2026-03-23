package com.goodee.beedan.dto.stock;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StockListDto {
    private Long stId;
    private String stBrNm;
    private String stNm;
    private String stCatNm;
    private BigDecimal stPr;
    private String stCur;
    private String stImgUrl;
}
