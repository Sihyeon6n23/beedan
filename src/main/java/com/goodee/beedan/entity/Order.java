package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ord_base_id")
    private Long ord_base_id;
    private String ord_base_nm;
    private String ord_base_rcv_nm;
    private String ord_base_adr_da;
    private String ord_base_adr_dt;
    private String ord_base_msg;
}
