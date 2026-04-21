package com.goodee.beedan.dto.buyer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerDto {

    private Long byId;
    private String memBizNo; // 사업자번호
    private String bgpGr;   // 고객

    private List<FeePolicyDto> feePolicies;

}
