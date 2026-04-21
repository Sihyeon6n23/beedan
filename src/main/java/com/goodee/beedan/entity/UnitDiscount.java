package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.OverlapType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UnitDiscount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "un_d_id")
    private Long unDId;
    @Column(name = "un_g_id")
    private Long unGId; // 묶음 단위 아이디
    @Column(name = "un_d_min_qn")
    private Integer unDMinQn;   // 최소 묶음 수량
    @Column(name = "un_d_min_am", precision = 18, scale = 0)
    private BigDecimal unDMinAm;    // 최소 금액
    @Column(name = "un_d_qn_dr", precision = 18, scale = 0)
    private BigDecimal unDQnDr; // 수량 할인율 (0.03=3%)
    @Column(name = "un_d_am_dr", precision = 18, scale = 0)
    private BigDecimal unDAmDr; // 금액 할인율
    @Column(name = "un_d_ov_ty", length = 20)
    private OverlapType unDOvTy; // 중복 충족 처리방식 HIGHER LOWER
    @Column(name = "un_d_des", columnDefinition = "TEXT")
    private String unDDes;  // 관리자 메모
    @CreatedDate
    @Column(name = "un_d_cr_dt", updatable = false, nullable = false)
    private LocalDateTime unDCrDt;
    @LastModifiedDate
    @Column(name = "un_d_up_dt", nullable = false)
    private LocalDateTime unDUpDt;

    @Builder
    public UnitDiscount(
            Long unitGroupId,
            Integer minQuantity,
            BigDecimal minAmount,
            BigDecimal quantityDiscountRate,
            BigDecimal amountDiscountRate,
            OverlapType overlapType,
            String description
    ){
        this.unGId = unitGroupId;
        this.unDMinQn = minQuantity;
        this.unDMinAm = minAmount;
        this.unDQnDr = quantityDiscountRate;
        this.unDAmDr = amountDiscountRate;
        this.unDOvTy = overlapType;
        this.unDDes = description;
    }

    public boolean isQuantityConditionMet(Integer unitCount){
        if (this.unDMinQn == null) return false;
        return unitCount >= this.unDMinQn;
    }

    public boolean isAmountConditionMet(BigDecimal totalAmount){
        if(this.unDMinAm == null) return false;
        return totalAmount.compareTo(this.unDMinAm) >= 0;
    }


    // 최종 할인율 계산
    public BigDecimal calculateFinalDiscountRate(
            Integer unitCount,
            BigDecimal totalAmount
    ){
        boolean qtyMet = isQuantityConditionMet(unitCount);
        boolean amtMet = isAmountConditionMet(totalAmount);

        BigDecimal qtyRate = qtyMet && this.unDQnDr != null
                ? this.unDQnDr : BigDecimal.ZERO;
        BigDecimal amtRate = amtMet && this.unDAmDr != null
                ? this.unDAmDr : BigDecimal.ZERO;

        if (
                qtyRate.compareTo(BigDecimal.ZERO) == 0
            && amtRate.compareTo(BigDecimal.ZERO) == 0
        ){
            return BigDecimal.ZERO;
        }

        //TODO 이것도 이넘으로 바꿔라 시현아
        return switch (this.unDOvTy) {
            case HIGHER -> qtyRate.max(amtRate);
            case LOWER -> qtyRate.min(amtRate);
            case MULTIPLY -> qtyRate.add(amtRate);
            case FIXED -> this.unDQnDr != null
                    ? this.unDQnDr : BigDecimal.ZERO;
        };


    }




}
