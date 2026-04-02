package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "ORDER_ITEM")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ordItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ord_base_id")
    private Order order;

    @Column(name = "ord_item_qn")
    private Integer ordItemQn; // 총 주문 수량

    @Column(name = "ord_item_am")
    private BigDecimal ordItemAm; // 결제 금액

    @Column(name = "ord_item_cre_dt")
    @CreatedDate
    private LocalDateTime ordItemCreDt;

    @Column(name = "ord_item_upd_dt")
    @LastModifiedDate
    private LocalDateTime ordItemUpdDt;

    private String ordItemStNm;
}
