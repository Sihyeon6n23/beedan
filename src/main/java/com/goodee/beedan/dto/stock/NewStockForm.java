package com.goodee.beedan.dto.stock;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class NewStockForm {
    @NotBlank
    private String stCd;
    @NotBlank
    private String stNm;
    @NotBlank
    private String brNm;
    @NotBlank
    private String catNm;
    @NotNull @DecimalMin("0")
    private BigDecimal stPr;
    @NotBlank
    private String stCur;
    private Long stReqMemId;
    private boolean stReqYn;
    private List<MultipartFile> newFiles;
}
