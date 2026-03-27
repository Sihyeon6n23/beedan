package com.goodee.beedan.dto.quote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCustomsRateRequest {

    @NotBlank(message = "비용 종류는 필수입니다")
    private String pcrTy;               // PORT | CUSTOMS | HS_CODE

    @NotNull(message = "소형 금액은 필수입니다")
    private BigDecimal pcrSmAm;

    @NotNull(message = "중형 금액은 필수입니다")
    private BigDecimal pcrMdAm;

    @NotNull(message = "대형 금액은 필수입니다")
    private BigDecimal pcrLgAm;

    private String pcrDes;
}