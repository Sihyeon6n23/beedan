package com.goodee.beedan.dto.root.sales;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SalesKpiResponse {

    private BigDecimal totalRevenue;        // 총 매출
    private long totalOrders;               // 총 주문 수
    private BigDecimal avgOrderAmount;      // 평균 주문 금액
    private long newCustomers;              // 신규 고객 수
}
