package com.goodee.beedan.dto.crawling;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UrlForm {
    @NotBlank(message = "Url을 입력하세요.")
    private String urlUrl;
    @NotBlank(message = "브랜드명을 입력하세요.")
    private String brNm;
    @NotBlank(message = "카테고리를 입력하세요.")
    private String catNm;
}
