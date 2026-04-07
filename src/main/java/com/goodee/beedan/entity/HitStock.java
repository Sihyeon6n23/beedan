package com.goodee.beedan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HitStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hitId;
    private Long stId; // 상품 아이디
    private LocalDateTime hitDt; // 등록 시간
}
