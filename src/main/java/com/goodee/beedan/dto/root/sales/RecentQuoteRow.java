package com.goodee.beedan.dto.root.sales;

import com.goodee.beedan.common.constant.QuoteStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class RecentQuoteRow {

    private String quoteCd;             // 견적번호
    private String buyerName;           // 고객사명
    private QuoteStatus status;         // 상태
    private BigDecimal amount;          // 금액
    private LocalDateTime submitDate;   // 제출일
    private String curatorName;         // 담당 큐레이터
}
