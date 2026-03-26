package com.goodee.beedan.dto.order;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderDto {
    private Long ord_base_id;
    private String ord_base_nm;
    private String ord_base_rcv_nm;
    private String ord_base_adr_da;
    private String ord_base_adr_dt;
    private String ord_base_msg;
}
