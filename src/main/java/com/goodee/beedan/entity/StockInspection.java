package com.goodee.beedan.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "STOCK_INSPECTION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stiId;
    private String stiNm;
    @Column(precision = 18, scale = 0)
    private BigDecimal stiAm;
    @Column(columnDefinition = "TEXT")
    private String stiDes;
    private Boolean stiYn;
    @CreatedDate
    private LocalDateTime stiCrDt;
    @LastModifiedDate
    private LocalDateTime stiUpDt;

    @Builder
    public StockInspection(String name, BigDecimal amount, String description) {
        this.stiNm = name;
        this.stiAm = amount;
        this.stiDes = description;
    }

    @PrePersist
    protected void onCreate() {
        if(this.stiYn != true) this.stiYn = true;
    }

    public void update(String name, BigDecimal amount, String description) {
        this.stiNm = name;
        this.stiAm = amount;
        this.stiDes = description;
    }

    public void deactivate() { this.stiYn = false; }
}