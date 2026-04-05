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
    private Long ordBaseId;

    private Boolean shCanYn;
    private String shRcvNm;
    private String shAdr;
    private String shAdrDt;

    private List<ShipmentItemDto> items;
}