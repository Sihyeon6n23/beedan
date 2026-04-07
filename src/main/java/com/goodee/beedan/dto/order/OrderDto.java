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

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class OrderDto {
    private Long ordBaseId;
    private String ordBaseRcvNm;
    private String ordBaseAdr;
    private String ordBaseAdrDt;
    private String ordBaseMsg;
    private String ordBaseNo; // 주문 번호 QU + 4글자
    private BigDecimal ordBaseTtAm;
    private OrderStatus ordBaseStt;
    private LocalDateTime ordBaseCreDt;

    private String ordSummaryNm;
    private List<ShipmentRequestDto> shipmentRequests; // 주문 생성시 이용
    private List<ShipmentResponseDto> shipmentResponses; // 주문 상세 조회용

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
        private Boolean shCanYn;
        private List<ShipmentItemResponseDto> shipmentItems;

        public String getShCarNm() {
            if (this.shCarCd == null || this.shCarCd.isEmpty()) {
                return "미정";
            }
            switch (this.shCarCd) {
                case "kr.cjlogistics": return "CJ대한통운";
                case "kr.epost":       return "우체국택배";
                case "kr.hanjin":      return "한진택배";
                case "kr.lotteglogis": return "롯데택배";
                case "kr.logen":       return "로젠택배";
                case "kr.cvsnet":      return "GS25 편의점택배";
                case "kr.cupost":      return "CU 편의점택배";
                default:               return this.shCarCd;
            }
        }
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