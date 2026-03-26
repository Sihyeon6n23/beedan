package com.goodee.beedan.dto.quote;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryRequest {

    private Long brId;                  // 브랜드 아이디

    @NotBlank(message = "공장명은 필수입니다")
    private String faNm;                // 공장명

    private String faAd;                // 공장 주소

    private String faCty;               // 공장 도시

    @NotBlank(message = "국가코드는 필수입니다")
    private String faCCd;               // 국가코드 (JP, CN, US 등)
}