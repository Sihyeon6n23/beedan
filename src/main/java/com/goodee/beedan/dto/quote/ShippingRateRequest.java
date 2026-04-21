package com.goodee.beedan.dto.quote;

import com.goodee.beedan.common.constant.TransportType;
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
public class ShippingRateRequest {

    @NotBlank(message = "출발 국가코드는 필수입니다")
    private String srCCd;                   // JP | CN | US

    @NotNull(message = "운송 수단은 필수입니다")
    private TransportType srTrspTy;         // SEA | AIR | EXPRESS

    @NotNull(message = "소형 기준 유닛수는 필수입니다")
    private Integer srSmQn;

    @NotNull(message = "소형 운임비는 필수입니다")
    private BigDecimal srSmAm;

    @NotNull(message = "중형 기준 유닛수는 필수입니다")
    private Integer srMdQn;

    @NotNull(message = "중형 운임비는 필수입니다")
    private BigDecimal srMdAm;

    @NotNull(message = "대형 기준 유닛수는 필수입니다")
    private Integer srLgQn;

    @NotNull(message = "대형 운임비는 필수입니다")
    private BigDecimal srLgAm;

    private String srDes;                   // 관리자 메모
}