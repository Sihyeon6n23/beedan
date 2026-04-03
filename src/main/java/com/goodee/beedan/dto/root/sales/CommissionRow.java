package com.goodee.beedan.dto.root.sales;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CommissionRow {

    private LocalDateTime date;         // 일자
    private String quoteCd;             // 견적번호
    private String buyerName;           // 고객사명
    private String grade;               // 등급
    private BigDecimal itemAmount;      // 물품 대금
    private BigDecimal serviceFee;      // 서비스 수수료
    private BigDecimal shippingFee;     // 배송 수수료
    private BigDecimal totalFee;        // 수수료 합계
}
