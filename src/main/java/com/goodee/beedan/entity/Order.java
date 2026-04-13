package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data @Builder
@RequiredArgsConstructor @AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ORDER_BASE")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ord_base_id")
    private Long ordBaseId;
    @Column(name = "ord_base_rcv_nm")
    private String ordBaseRcvNm;
    @Column(name = "ord_base_adr")
    private String ordBaseAdr;
    @Column(name = "ord_base_adr_dt")
    private String ordBaseAdrDt;
    @Column(name = "ord_base_msg")
    private String ordBaseMsg;
    @Column(name = "ord_base_no")
    private String ordBaseNo;
    @Column(name = "ord_base_stt")
    @Enumerated(EnumType.STRING)
    private OrderStatus ordBaseStt;
    @Column(name = "ord_base_cre_dt")
    @CreatedDate
    private LocalDateTime ordBaseCreDt;
    @Column(name = "ord_base_upd_dt")
    @LastModifiedDate
    private LocalDateTime ordBaseUpdDt;
    @Column(name = "ord_base_tt_am", precision = 18, scale = 0)
    private BigDecimal ordBaseTtAm; // 주문 총 금액

    @ManyToOne
    @JoinColumn(name = "mem_id")
    private Member member;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Shipment> shipments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void validateOwner(Long requesterId) {
        if (!this.member.getMemId().equals(requesterId)) {
            throw new IllegalArgumentException("본인의 주문만 조회할 수 있습니다.");
        }
    }
}
