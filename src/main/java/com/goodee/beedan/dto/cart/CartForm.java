package com.goodee.beedan.dto.cart;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartForm {
    private Long stId;
    private Long caStQn;
}
