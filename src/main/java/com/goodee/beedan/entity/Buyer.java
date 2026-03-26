package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BUYER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Buyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long byId;
    @Column(length = 50, nullable = false)
    private String memBizNo;
    private String bgpGr;   // 등급
    private String memBizTtl; // 상호명
    private Integer byOrdCnt; // 누적 거래 횟수
    @Column(precision = 18, scale = 0)
    private BigDecimal byTtlAm; // 누적 거래 금액
    private LocalDateTime byFrDt; // 최초 거래일
    private LocalDateTime byLtDt; // 최근 거래일

    public void recordOrder(BigDecimal orderAmountKrw){
        this.byOrdCnt = (this.byOrdCnt == null ? 0 : this.byOrdCnt) + 1;
        this.byTtlAm = (this.byTtlAm == null ? BigDecimal.ZERO : this.byTtlAm)
                .add(orderAmountKrw);
        this.byLtDt = LocalDateTime.now();
        if (this.byFrDt == null) this.byFrDt = LocalDateTime.now();
    }

    public void updateGrade(String newGrade){
        this.bgpGr = newGrade;
    }

    @PrePersist
    protected void onCreate() {
        if (this.byOrdCnt == null) this.byOrdCnt = 0;
        if (this.byTtlAm == null) this.byTtlAm = BigDecimal.ZERO;
    }


}
