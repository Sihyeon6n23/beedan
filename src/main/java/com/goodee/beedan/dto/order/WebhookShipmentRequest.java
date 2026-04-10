package com.goodee.beedan.dto.order;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WebhookShipmentRequest {  // shTraNo, shCarNo, quId, shHblNo 필요
    private Long quId;
    private Long pyId;

    private String shTraNo;
    private String shCarNo;
    private String shStaAdr;
    private String shRcvNm;
    private String shHblNo;
}
