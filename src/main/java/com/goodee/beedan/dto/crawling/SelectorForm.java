package com.goodee.beedan.dto.crawling;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SelectorForm {
    private String selItem;
    private String selNm;
    private String selPr;
    private String selImg;
    private String currency;
    private String brNm;
    private String catNm;
}
