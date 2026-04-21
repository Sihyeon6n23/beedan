package com.goodee.beedan.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class ShipmentItemDto {
    private Long shItemId;
    private String ordItmNm;
    private Integer shQn;
    private Long prodId;
}
