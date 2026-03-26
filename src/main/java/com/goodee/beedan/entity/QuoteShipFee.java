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
@Table(name = "QU_SHIP_FEE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QuoteShipFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qsfId;
    private Long quInfoId;  //QuoteInfo
    private Long quId;  //QuoteBase
    private Long ngId;  //Negotiation
    private Long faId;  //Factory
    private String qsfFaNm; //공장 이름
    private String qsfFaCCd;// 공장 국가 코드
    private String qsfTrspTy;   // 운송 수단 SEA AIR EXPRESS
    private Integer qsfTtDz; // 총 다스 수량
    @Column(precision = 18, scale = 0)
    private BigDecimal qsfSrAm; // 해외 운임비
    private Boolean qsfSrYn;    // 수동 운임비 조정 여부
    private String qsfSrDes;    // 수동 수정 사유
    @Column(precision = 18, scale = 0)
    private BigDecimal qsfPrtAm;    // 항만 비용
    @Column(precision = 18, scale = 0)
    private BigDecimal qsfCstAm;    // 통관 수수료
    @Column(precision = 18, scale = 0)
    private BigDecimal qsfHsCd;     // HS Code 신고료
    private Boolean qsfInsYn;    // 보험 가입 여부
    @Column(precision = 18, scale = 0)
    private BigDecimal qsfInsAm;    // 보험료
    @Column(precision = 18, scale = 0)
    private BigDecimal qsfCifAm;    // CIF 금액(상품+운임+보험)
    @Column(precision = 10, scale = 4)
    private BigDecimal qsfDtyR;     // 관세율
    @Column(precision = 18, scale = 0)
    private BigDecimal qsfDty;      // 관세액
    @Column(precision = 18, scale = 0)
    private BigDecimal qsfVat;      // 부가세액
    @Column(precision = 10, scale = 4)
    private BigDecimal qsfDscR;     // 배송비 할인율
    @Column(precision = 18, scale = 0)
    private BigDecimal qsfDscAm;    // 배송비 할인 금액
    @Column(precision = 18, scale = 0)
    private BigDecimal qsfTtl;      // 공장별 배송비 최종 합계
    @CreatedDate
    private LocalDateTime qsfCrDt;
    @LastModifiedDate
    private LocalDateTime qsfUpDt;

    @Builder
    public QuoteShipFee(
            Long quoteInfoId,
            Long quoteId,
            Long negoId,
            Long factoryId,
            String factoryName,
            String factoryCountryCode,
            String transportType,
            Integer totalDozen
    ){
        this.quInfoId = quoteInfoId;
        this.quId = quoteId;
        this.ngId = negoId;
        this.faId = factoryId;
        this.qsfFaNm = factoryName;
        this.qsfFaCCd = factoryCountryCode;
        this.qsfTrspTy = transportType;
        this.qsfTtDz = totalDozen;
        this.qsfSrYn = false;
        this.qsfInsYn = false;
    }

    public void setShippingFee(BigDecimal amount){
        this.qsfSrAm = amount;
        this.qsfSrYn = false;
    }

    public void overrideShippingFee(BigDecimal amount, String reason){
        this.qsfSrAm = amount;
        this.qsfSrYn = true;
        this.qsfSrDes = reason;
    }

    public void setPortCustomsFee(
            BigDecimal portFee,
            BigDecimal customsFee,
            BigDecimal hsCodeFee
    ){
        this.qsfPrtAm = portFee;
        this.qsfCstAm = customsFee;
        this.qsfHsCd = hsCodeFee;
    }

    public void setInsurance(BigDecimal insuranceFee){
        this.qsfInsYn = true;
        this.qsfInsAm = insuranceFee;
    }

    public void calculateCif(BigDecimal itemTotal){
        BigDecimal insurance = this.qsfInsAm != null
                ? this.qsfInsAm
                : BigDecimal.ZERO;
        this.qsfCifAm = itemTotal
                .add(this.qsfSrAm != null
                        ? this.qsfInsAm
                        : BigDecimal.ZERO)
                .add(insurance);
    }

    public void calculateDutyAndVat(BigDecimal dutyRate){
        this.qsfDtyR = dutyRate;
        this.qsfDty = this.qsfCifAm
                .multiply(dutyRate)
                .setScale(0, RoundingMode.HALF_UP);
        this.qsfVat = this.qsfCifAm
                .add(this.qsfDty)
                .multiply(new BigDecimal("0.10"))
                .setScale(0, RoundingMode.HALF_UP);
    }

    public void applyDiscount(BigDecimal discountRate){
        this.qsfDscR = discountRate;
        this.qsfDscAm = this.qsfTtl != null
                ? this.qsfTtl.multiply(discountRate)
                    .setScale(0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    public void calculateTotal(){
        BigDecimal subtotal = BigDecimal.ZERO;
        subtotal = subtotal
                .add(this.qsfSrAm   != null ? this.qsfSrAm : BigDecimal.ZERO)
                .add(this.qsfPrtAm  != null ? this.qsfPrtAm : BigDecimal.ZERO)
                .add(this.qsfCstAm  != null ? this.qsfCstAm : BigDecimal.ZERO)
                .add(this.qsfHsCd   != null ? this.qsfHsCd : BigDecimal.ZERO)
                .add(this.qsfInsAm  != null ? this.qsfInsAm : BigDecimal.ZERO)
                .add(this.qsfDty    != null ? this.qsfDty : BigDecimal.ZERO)
                .add(this.qsfVat    != null ? this.qsfVat : BigDecimal.ZERO);

        applyDiscount(this.qsfDscR != null ? this.qsfDscR : BigDecimal.ZERO);;
    }
}
