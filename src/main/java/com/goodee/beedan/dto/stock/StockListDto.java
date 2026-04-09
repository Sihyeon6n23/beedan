package com.goodee.beedan.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockListDto {
    private Long stId;
    private String stBrNm;
    private String stNm;
    private String stCatNm;
    private BigDecimal stPr;
    private String stCur;
    private String stImgUrl;
    private boolean wished;
}
