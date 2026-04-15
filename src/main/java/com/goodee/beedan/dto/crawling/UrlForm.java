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
    @NotBlank
    private String urlUrl;
    @NotBlank
    private String brNm;
    @NotBlank
    private String catNm;
}
