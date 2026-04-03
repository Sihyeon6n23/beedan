package com.goodee.beedan.dto.order;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.common.constant.ShipmentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long ordBaseId;
    private String ordBaseRcvNm;
    private String ordBaseAdr;
    private String ordBaseAdrDt;
    private String ordBaseMsg;
    private String ordBaseNo;
    private BigDecimal ordBaseTtAm;
    @Enumerated(EnumType.STRING)
    private OrderStatus ordBaseStt;
    private LocalDateTime ordBaseCreDt;

    private List<ShipmentRequestDto> shipmentRequests; // 주문 생성시 이용
    private List<ShipmentResponseDto> shipmentResponses; // 주문 상세 조회용
    private String ordSummaryNm;

    @Data
    public static class ShipmentRequestDto {
        private String shRcvNm;
        private String shAdr;
        private String shAdrDt;
        private String shMsg;
        private List<ShipmentItemRequestDto> items;
    }

    @Data
    public static class ShipmentItemRequestDto {
        private Long quDtId;
        private Integer shQn;
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class ShipmentResponseDto {
        private Long shId;
        private String shTraNo;
        private String shCarCd;
        private ShipmentStatus shStt;
        private String shRcvNm;
        private String shAdr;
        private String shAdrDt;
        private String shMsg;
        private List<ShipmentItemResponseDto> shipmentItems;
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class ShipmentItemResponseDto {
        private Long shItemId;
        private String ordItmNm;
        private Integer shQn;
        private Long prodId;
    }
}