package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.ShipmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "SHIPMENT_ITEM")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ShipmentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sh_item_id")
    private Long shItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sh_id")
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ord_item_id")
    private OrderItem orderItem;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus shItemStt;

    @Column(name = "sh_qn")
    private Integer shQn;
}
