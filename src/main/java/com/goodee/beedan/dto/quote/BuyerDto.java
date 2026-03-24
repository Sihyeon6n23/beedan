package com.goodee.beedan.dto.quote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerDto {

    private Long byId;
    private String memBizNo; // 사업자번호
    private String bgpGr;   // 고객


}
