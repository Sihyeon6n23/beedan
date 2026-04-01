package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QuoteDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quDtId;
    private Long quInfoId; // QuoteInfo
    private Long quId;  //QuoteBase
    private Long ngId;  //Negotiation
    private Long stId;  //Stock
    private String stNm; // 상품 이름
    private Integer quDtQn;  // 상품 수량
    private Long faId;  //Factory
    private String faNm;    //공장 이름
    @Column(name = "un_g_id")
    private Long unGId; // 묶음 단위 아이디
    @Column(name = "un_g_nm")
    private String unGNm;   // 묶음 단위명
    @Column(name = "qu_u_qn")
    private Integer quUQn;  // 묶음 수량

    @Column(precision = 18, scale = 4)
    private BigDecimal quDtFgPr;    // 외화 단가
    @Column(precision = 18, scale = 0)
    private BigDecimal quDtKrPr;    // 원화 단가
    @Column(precision = 18, scale = 0)
    private BigDecimal quDtPr;      // 원화 합계

    private String quDtRe;  // 비고
    private Long rcId;  // 수령지 ID

    @Builder
    public QuoteDetail(
            Long quoteInfoId,
            Long quoteId,
            Long negoId,
            Long stockId,
            Integer stockQuantity,
            String stockName,
            Long factoryId,
            String factoryName,
            Long unitGroupId,
            String unitGroupName,
            Integer unitGroupQuantity,
            BigDecimal foreignPrice,
            BigDecimal krwTotal,
            Long receiverId
    ){
        this.quInfoId = quoteInfoId;
        this.quId = quoteId;
        this.ngId = negoId;
        this.stId = stockId;
        this.quDtQn = stockQuantity;
        this.stNm = stockName;
        this.faId = factoryId;
        this.faNm = factoryName;
        this.unGId = unitGroupId;
        this.unGNm = unitGroupName;
        this.quUQn = unitGroupQuantity;
        this.quDtFgPr = foreignPrice;
        this.quDtPr = krwTotal;
        this.rcId = receiverId;
    }

    public void calculateKrwPrice(BigDecimal exchangeRate){
        this.quDtKrPr = this.quDtFgPr
                .multiply(exchangeRate)
                .setScale(0, RoundingMode.HALF_UP);
        this.quDtPr = this.quDtKrPr
                .multiply(BigDecimal.valueOf(this.quDtQn))
                .setScale(0, RoundingMode.HALF_UP);
    }

    public Long getFactoryId(){
        return this.faId;
    }
    public Integer getTotalUnitCount(){
        return this.quUQn;
    }

}
