package com.goodee.beedan.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartDto {
    // Cart 정보
    private Long caId; // 카트 상세 id
    private Long caStQn; // 상품 수량

    // Member 정보
    private Long memId; // 회원 id

    // Stock 정보
    private Long stId; // 상품 아이디
    private String stCd; // 상품 코드
    private String stBrNm; // 브랜드명
    private String stNm; // 상품명
    private BigDecimal stPr; // 상품 가격
    private BigDecimal stKrwPr; // 원화 환산 가격
    private String stCur; // 상품 통화
    private String stImgUrl; // 상품 이미지 url
    private boolean stUseYn; // 상품 사용 여부

}
