package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BUYER_GRADE_POLICY")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BuyerGradePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bgpId;
    private String bgpGr; // STANDARD PREMIUM VIP
    private Integer bgpMinOrdCnt; // 최소 거래 횟수 (null=미적용)
    private BigDecimal bgpMinTtAm; // 최소 누적 금액 (null=미적용)
    private LocalDateTime bgpEfFrDt; // 정책 시작일
    private LocalDateTime bgpEfToDt; // 정책 종료일 (null=무기한)
    @Column(columnDefinition = "TEXT")
    private String bgpDes; // 등급 설명
    @Builder.Default
    @Column(nullable = false)
    private Boolean bgpAcYn = true; // 활성 여부
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime bgpCrDt;
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime bgpUpDt;

    public void deactivate() {
        this.bgpAcYn = false;
    }

    public void update(
            Integer minOrderCount,
            BigDecimal minTotalAmout,
            LocalDateTime effectFromDate,
            LocalDateTime effectToDate,
            String description
    ){
        this.bgpMinOrdCnt = minOrderCount;
        this.bgpMinTtAm = minTotalAmout;
        this.bgpEfFrDt = effectFromDate;
        this.bgpDes = description;
    }

    public boolean isValid() {
        if (!Boolean.TRUE.equals(this.bgpAcYn)) return false;

        LocalDateTime now = LocalDateTime.now();
        if (this.bgpEfFrDt != null && now.isBefore(this.bgpEfFrDt)) return false;
        if (this.bgpEfToDt != null && now.isAfter(this.bgpEfToDt)) return false;

        return true;
    }
}
