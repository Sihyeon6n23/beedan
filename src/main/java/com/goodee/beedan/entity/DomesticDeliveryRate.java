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
import java.time.LocalDateTime;

@Entity
@Table(name = "DOMESTIC_DELIVERY_RATE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DomesticDeliveryRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ddrId;
    @Column(unique = true)
    private String ddrRgn;  // 지역 구분 SEOUL GYEONGGI METRO PROVINCE
    @Column(precision = 18, scale = 0)
    private BigDecimal ddrAm;   // 기본 배송비
    @Column(name = "ddr_e_am", precision = 18, scale = 0)
    private BigDecimal ddrEAm;  // 추가 배송비
    private String ddrDes;  // 관리자 메모
    private Boolean ddrYn;
    @CreatedDate
    private LocalDateTime ddrCrDt;
    @LastModifiedDate
    private LocalDateTime ddrUpDt;

    @PrePersist
    protected void onCreate(){
        if (this.ddrYn == null) this.ddrYn = true;
        if (this.ddrEAm == null) this.ddrEAm = BigDecimal.ZERO;

    }

    public void deactivate() {
        this.ddrYn = false;
    }

    @Builder
    public DomesticDeliveryRate(
            String region,
            BigDecimal amount,
            BigDecimal extraAmount,
            String description
    ){
        this.ddrRgn = region;
        this.ddrAm = amount;
        this.ddrEAm = extraAmount != null ? extraAmount : BigDecimal.ZERO;
        this.ddrYn = true;
        this.ddrDes = description;
    }

    public void update(
            BigDecimal amount,
            BigDecimal extraAmount,
            String description
    ){
        this.ddrAm = amount;
        this.ddrEAm = extraAmount != null ? extraAmount : BigDecimal.ZERO;
        this.ddrDes = description;
    }

    public BigDecimal totalAmount() {
        BigDecimal extra = this.ddrEAm != null ? this.ddrEAm : BigDecimal.ZERO;
        return this.ddrAm.add(extra);
    }

}
