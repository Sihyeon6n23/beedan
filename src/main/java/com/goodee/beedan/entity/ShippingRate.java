package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.TransportType;
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
@Table(name = "SHIPPING_RATE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ShippingRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long srId;
    @Column(name = "sr_c_cd", length = 10)
    private String srCCd;   //출발 국가코드
    @Enumerated(EnumType.STRING)
    private TransportType srTrspTy;    // 운송 수단(SEA AIR EXPRESS)
    private Integer  srSmQn;  // 소형 기준 유닛 수
    @Column(precision = 18, scale = 0)
    private BigDecimal   srSmAm;  // 소형 운임비
    private Integer  srMdQn;  // 중형 기준 유닛 수
    @Column(precision = 18, scale = 0)
    private BigDecimal  srMdAm;  // 중형 운임비
    private Integer  srLgQn;  // 대형 기준 유닛 수
    @Column(precision = 18, scale = 0)
    private BigDecimal  srLgAm;  // 대형 운임비
    @Column(name = "sr_yn", columnDefinition = "BOOLEAN")
    private Boolean srYn;   // 활성 여부
    private String srDes;
    @CreatedDate
    private LocalDateTime srCrDt;
    @LastModifiedDate
    private LocalDateTime srUpDt;

    @Builder
    public ShippingRate(
            String countryCode,
            TransportType transportType,
            Integer smallQuantity, BigDecimal smallAmount,
            Integer mediumQuantity, BigDecimal mediumAmount,
            Integer largeQuantity, BigDecimal largeAmount,
            String description
            ){
        this.srCCd = countryCode;
        this.srTrspTy = transportType;
        this.srSmQn = smallQuantity;
        this.srSmAm = smallAmount;
        this.srMdQn = mediumQuantity;
        this.srMdAm = mediumAmount;
        this.srLgQn = largeQuantity;
        this.srLgAm = largeAmount;
        this.srYn = true;
        this.srDes = description;
    }

    @PrePersist
    protected void onCreate(){
        if (this.srYn == null) this.srYn = true;
    }

    public void deactivate() {
        this.srYn = false;
    }

    public void update(
            BigDecimal smallAmount,
            BigDecimal mediumAmount,
            BigDecimal largeAmount,
            String description
    ){
        this.srSmAm = smallAmount;
        this.srMdAm = mediumAmount;
        this.srLgAm = largeAmount;
        this.srDes = description;
    }

    public BigDecimal getApplicableAmount(Integer totalDozen){
        if (totalDozen >= this.srLgQn) return this.srLgAm;
        if (totalDozen >= this.srMdQn) return this.srMdAm;
        return this.srSmAm;
    }

    public String getSizeType(Integer totalDozen){
        if (totalDozen >= this.srLgQn) return "LARGE";
        if (totalDozen >= this.srMdQn) return "MEDIUM";
        return "SMALL";
    }




}
