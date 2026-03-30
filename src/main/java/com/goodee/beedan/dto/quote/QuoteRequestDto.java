package com.goodee.beedan.dto.quote;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class QuoteRequestDto {

    @NotEmpty(message = "견적 요청 상품이 없습니다")
    @Valid
    private List<QuoteRequestItemDto> items;

    @Getter
    @NoArgsConstructor
    public static class QuoteRequestItemDto {

        @NotNull(message = "상품 아이디는 필수입니다")
        private Long stId;

        @NotNull(message = "수량은 필수입니다")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        private Long qty;
    }
}
