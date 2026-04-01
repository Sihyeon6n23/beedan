package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.ShipmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sh_id")
    private Long shId;
    private String shTraNo;
	private String shCarCd;
    @Enumerated
    private ShipmentStatus shStt;
    @CreatedDate
    private LocalDateTime shCreDt;
    @LastModifiedDate
    private LocalDateTime shUpdDt;
    @ManyToOne
    @JoinColumn(name="ord_base_id")
    private Order order;

    private Boolean sh_can_yn;
}
