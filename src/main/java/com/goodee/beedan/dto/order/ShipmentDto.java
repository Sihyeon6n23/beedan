package com.goodee.beedan.dto.order;

import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.entity.Order;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Builder
public class ShipmentDto {
    private Long shId;
    private Long shTraNo;
    private String shCarCd;
    private ShipmentStatus shStt;
    private LocalDateTime shCreDt;
    private LocalDateTime shUpdDt;
    private Order order;
}
