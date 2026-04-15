package com.goodee.beedan.dto.crawling;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@NotBlank
public class SelectorForm {
    private String selItem;
    private String selNm;
    private String selPr;
    private String selImg;
    @NotBlank
    private String currency;
    @NotBlank
    private String brNm;
    @NotBlank
    private String catNm;
}
