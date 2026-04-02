package com.goodee.beedan.dto.order;

import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.entity.Order;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ShipmentDto {
    private Long shId;
    private String shTraNo;
    private String shCarCd;
    private ShipmentStatus shStt;
    private LocalDateTime shCreDt;
    private LocalDateTime shUpdDt;

    // Order 엔티티 대신 ID만 전달 (순환 참조 방지)
    private Long ordBaseId;

    // 엔티티에 추가된 필드들 반영
    private Boolean shCanYn;
    private String shRcvNm;
    private String shAdr;
    private String shAdrDt;

    // 이 배송지에 포함된 아이템 리스트 (다중 배송 상세 조회용)
    private List<ShipmentItemDto> items;
}