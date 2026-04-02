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

    // 1. [요청용] 주문 생성 시 프론트에서 보내주는 데이터 리스트
    private List<ShipmentRequestDto> shipmentRequests;

    // 2. [응답용] 주문 상세 조회 시 DB에서 꺼내서 담아주는 데이터 리스트
    private List<ShipmentResponseDto> shipmentResponses;

    // --- Inner Classes (요청용) ---
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

    // --- Inner Classes (응답용) ---
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
        private Integer shQn;
        private Long prodId;
    }
}