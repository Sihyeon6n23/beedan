package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "hs_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HsCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hs_id")
    private Long hsId;

    @Column(name = "cat_id", nullable = false, unique = true)
    private Long catId;

    @Column(name = "hs_cd", nullable = false, length = 20)
    private String hsCd;

    @Column(name = "hs_nm", nullable = false, length = 100)
    private String hsNm;

    @Column(name = "hs_du_ra", nullable = false, precision = 5, scale = 4)
    private BigDecimal hsDuRa;

    @Column(name = "hs_des", length = 255)
    private String des;

    @Builder
    public HsCode(Long categoryId, String code, String name,
                  BigDecimal dutyRate, String description) {
        this.catId = categoryId;
        this.hsCd       = code;
        this.hsNm       = name;
        this.hsDuRa   = dutyRate;
        this.des = description;
    }

    // 비즈니스 메서드

    /**
     * 관세 계산
     * @param cifAmount CIF 금액
     * @return 관세액
     */
    public BigDecimal calculateDuty(BigDecimal cifAmount) {
        return cifAmount.multiply(this.hsDuRa);
    }

    /**
     * 부가세 계산
     * @param cifAmount CIF 금액
     * @return 부가세액
     */
    public BigDecimal calculateVat(BigDecimal cifAmount) {
        BigDecimal duty = calculateDuty(cifAmount);
        return cifAmount.add(duty).multiply(new BigDecimal("0.10"));
    }

    /**
     * 관세 + 부가세 합계
     * @param cifAmount CIF 금액
     * @return 총 세금
     */
    public BigDecimal calculateTotalTax(BigDecimal cifAmount) {
        return calculateDuty(cifAmount).add(calculateVat(cifAmount));
    }

    // 수정 메서드 (관리자용)
    public void updateCode(String code, String name,
                           BigDecimal dutyRate, String description) {
        this.hsCd        = code;
        this.hsNm        = name;
        this.hsDuRa    = dutyRate;
        this.des        = description;
    }
}