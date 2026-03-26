package com.goodee.beedan.dto.quote;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitGroupRequest {

    @NotBlank(message = "단위명은 필수입니다")
    private String unGNm;               // 다스, 박스, 롤

    @NotNull(message = "단위당 수량은 필수입니다")
    @Min(value = 1, message = "단위당 수량은 1 이상이어야 합니다")
    private Integer unGQn;              // 12, 24, 50
}