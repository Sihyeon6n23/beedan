package com.goodee.beedan.dto.stock;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStockDto {
    private Long stId; // 상품 아이디
    @NotBlank
    private String stCd; // 상품 코드
    private Long brId; // 브랜드 id
    @NotBlank
    private String stBrNm; // 브랜드명
    private Long catId; // 카테고리 id
    @NotBlank
    private String stCatNm; // 카테고리명
    private String stNm; // 상품명
    @NotNull @DecimalMin("0")
    private BigDecimal stPr; // 상품 가격
    @NotBlank
    private String stCur; // 상품 통화
    private String stImgUrl; // 상품 이미지 url
    private boolean stExpYn; // 노출 여부
    private boolean stUseYn; // 사용 여부
    private boolean stDelYn; // 삭제 여부

    private boolean stReqYn; // 요청 상품 여부
    private Long stReqMemId; // 요청자 id

    private LocalDateTime stCraDt; // 크롤링 시간
    private LocalDateTime stCreDt; // 등록 시간(생성 시간)
    private LocalDateTime stUpdDt; // 수정 시간

    private Long stWisCnt; // 관심 수
    private Long stPurCnt; // 구매 횟수
}
