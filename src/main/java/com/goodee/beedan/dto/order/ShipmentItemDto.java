package com.goodee.beedan.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class ShipmentItemDto {
    private BigDecimal shQn;
    private Long sh_id;
}
