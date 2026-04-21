package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "ORDER_ITEM")
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ord_item_id")
    private Long ordItmId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ord_base_id")
    private Order order;

    @Column(name = "ord_item_qn")
    private Integer ordItmQn; // 총 주문 수량

    @Column(name = "ord_item_am")
    private BigDecimal ordItmAm; // 결제 금액

    @Column(name = "ord_item_cre_dt")
    @CreatedDate
    private LocalDateTime ordItmCreDt;

    @Column(name = "ord_item_upd_dt")
    @LastModifiedDate
    private LocalDateTime ordItmUpdDt;

    @Column(name = "ord_item_nm")
    private String ordItmNm;

    @Column(name="ord_item_thm_key")
    private String ordItmThumbKey;

    @Column(name="ord_item_img_url")
    private String ordItmStUrl;
}
