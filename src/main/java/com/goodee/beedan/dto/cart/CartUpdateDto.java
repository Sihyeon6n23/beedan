package com.goodee.beedan.dto.cart;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartUpdateDto {
    private Long caId;
    private Long caStQn;
}
