package com.goodee.beedan.dto.buyer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeePolicyDto {

    private Long fpId;
    private String fpFeeTy; // SERVICE_COMMISION, SHIPPING
    private String bgpGr;   // STANDARD, PREMIUM, VIP
    private String fpCalcTy;// RATE, FIXED
    private BigDecimal fpVal;// 5% or 50,000 etc



}
