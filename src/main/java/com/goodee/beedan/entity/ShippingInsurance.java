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
@Table(name = "SHIPPING_INSURANCE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long siId;
    private String siNm;
    @Column(precision = 10, scale = 4)
    private BigDecimal siAm;
    @Column(columnDefinition = "TEXT")
    private String siDes;
    private Boolean siYn;
    @CreatedDate
    private LocalDateTime siCrDt;
    @LastModifiedDate
    private LocalDateTime siUpDt;

    @PrePersist
    protected void onCreate(){
        if (this.siYn != null) this.siYn = true;
    }

    @Builder
    public ShippingInsurance(String name, BigDecimal amount, String description){
        this.siNm = name;
        this.siAm = amount;
        this.siDes = description;
    }

    public void update (String name, BigDecimal amount, String description) {
        this.siNm = name;
        this.siAm = amount;
        this.siDes = description;
    }
    public void deactivate() {this.siYn = false;}


}
