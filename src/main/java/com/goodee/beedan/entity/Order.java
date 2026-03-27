package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.OrderStatus;
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
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ord_base_id")
    private Long ordBaseId;
    private String ordBaseRcvNm;
    private String ordBaseAdr;
    private String ordBaseAdrDt;
    private String ordBaseMsg;
    private String ordBaseNo;
    @Enumerated(EnumType.STRING)
    private OrderStatus ordBaseStt;
    @CreatedDate
    private LocalDateTime ordBaseCreDt;
    @LastModifiedDate
    private LocalDateTime ordBaseUpdDt;

    @ManyToOne
    @JoinColumn(name = "mem_id")
    private Member member;
}
