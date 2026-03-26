package com.goodee.beedan.dto.quote;

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
public class NegotiationRequest {

    @NotBlank(message = "협상명은 필수입니다")
    private String ngNm;

    @NotNull(message = "회원 아이디는 필수입니다")
    private Long memId;

}