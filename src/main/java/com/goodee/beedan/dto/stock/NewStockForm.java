package com.goodee.beedan.dto.stock;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class NewStockForm {
    private String stCd;
    private String stNm;
    private String brNm;
    private String catNm;
    private BigDecimal stPr;
    private String stCur;
    private Long stReqMemId;
    private boolean stReqYn;
    private List<MultipartFile> newFiles;
}
