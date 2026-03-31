package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "EXCHANGE_RATE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long erId;

    @Column(nullable = false, length = 10)
    private String erCr;    //통화 "JPY"

    @Column(nullable = false, precision = 18, scale =6)
    private BigDecimal erRa;

    @Column(nullable = false)
    private LocalDateTime erFDt;    // 환율 기준 시간

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime erCrDt;   // 생성 기간

    @Builder
    public ExchangeRate(String currency, BigDecimal rate, LocalDateTime fetchedAt){
        this.erCr = currency;
        this.erRa = rate;
        this.erFDt = fetchedAt;
    }

    public BigDecimal toKrw(BigDecimal foreignAmount) {
        if (foreignAmount == null) return BigDecimal.ZERO;
        return foreignAmount
                .multiply(this.erRa)
                .setScale(0, RoundingMode.HALF_UP);
    }


}

