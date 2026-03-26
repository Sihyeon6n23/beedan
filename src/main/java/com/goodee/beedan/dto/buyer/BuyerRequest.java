package com.goodee.beedan.dto.buyer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerRequest {

    private String memBizNo;    // 상버자번호
    private String memBizTtl;   // 상호명
}
