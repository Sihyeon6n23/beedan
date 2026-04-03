package com.goodee.beedan.dto.root.sales;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GradeDistribution {

    private String grade;               // 등급명
    private BigDecimal revenue;         // 매출 합계
    private long orderCount;            // 주문 수
    private long buyerCount;            // 고객사 수
    private BigDecimal percentage;      // 비율 (%)
}
