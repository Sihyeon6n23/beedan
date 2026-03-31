package com.goodee.beedan.dto.order;

import com.goodee.beedan.common.constant.OrderStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderDto {
    private Long ordBaseId;
    private String ordBaseRcvNm;
    private String ordBaseAdr;
    private String ordBaseAdrDt;
    private String ordBaseMsg;
    private String ordBaseNo;
    @Enumerated(EnumType.STRING)
    private OrderStatus ordBaseStt;
    @CreatedDate
    private LocalDateTime ordBaseCreDt;
}
