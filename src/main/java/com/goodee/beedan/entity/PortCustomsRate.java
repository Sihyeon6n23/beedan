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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PortCustomsRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pcrId;
    private String pcrTy;   // 비용 종류 PORT CUSTOMS HSCODE
    @Column(precision = 18, scale = 0)
    private BigDecimal pcrSmAm; // 소형 금액
    @Column(precision = 18, scale = 0)
    private BigDecimal pcrMdAm; // 중형 금액
    @Column(precision = 18, scale = 0)
    private BigDecimal pcrLgAm; // 대형 금액
    private Boolean pcrYn;
    private String pcrDes;
    @CreatedDate
    private LocalDateTime pcrCrDt;
    @LastModifiedDate
    private LocalDateTime pcrUpDt;

    @PrePersist
    protected void onCreate() {
        if (this.pcrYn == null) this.pcrYn = true;
    }

    @Builder
    public PortCustomsRate(
            String type,
            BigDecimal smallAmount,
            BigDecimal mediumAmount,
            BigDecimal largeAmount,
            String description
    ){
        this.pcrTy = type;
        this.pcrSmAm = smallAmount;
        this.pcrMdAm = mediumAmount;
        this.pcrLgAm = largeAmount;
        this.pcrYn = true;
        this.pcrDes = description;
    }

    public void deactivate() {
        this.pcrYn = false;
    }

    public void update(
            BigDecimal smallAmount,
            BigDecimal mediumAmount,
            BigDecimal largeAmount,
            String description
    ){
        this.pcrSmAm = smallAmount;
        this.pcrMdAm = mediumAmount;
        this.pcrLgAm = largeAmount;
        this.pcrDes = description;
    }

    // 사이즈 타입에 따른 금액 반환
    public BigDecimal getApplicableAmount(String sizeType){
        return switch (sizeType) {
            case "LARGE" -> this.pcrLgAm;
            case "MEDIUM" -> this.pcrMdAm;
            default -> this.pcrSmAm;
        };
    }


}
