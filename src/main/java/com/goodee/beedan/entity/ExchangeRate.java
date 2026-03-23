package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rate")
@Data
@RequiredArgsConstructor
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long erId;

    @Column(nullable = false, length = 10)
    private String erCr;    //통화 "JPY"

    @Column(nullable = false, precision = 18, scale =6)
    private BigDecimal erRa;

    @Column(nullable = false)
    private LocalDateTime crFDt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime erCrDt;

    @Builder
    public ExchangeRate(String currency, BigDecimal rate, LocalDateTime fetchedAt){
        this.erCr = currency;
        this.erRa = rate;
        this.crFDt = fetchedAt;
        this.erCrDt = LocalDateTime.now();
    }
}
