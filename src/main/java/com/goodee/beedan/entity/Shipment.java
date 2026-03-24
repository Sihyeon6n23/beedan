package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.ShipmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shi_id;
    private Long shi_tra_no;
	private String shi_ca_cd;
    @Enumerated
    private ShipmentStatus shi_stt;
    @CreatedDate
    private LocalDateTime shi_base_cre_dt;
    @LastModifiedDate
    private LocalDateTime shi_div_dt;
    @ManyToOne
    @JoinColumn(name="ord_base_id")
    private Order order;
}
