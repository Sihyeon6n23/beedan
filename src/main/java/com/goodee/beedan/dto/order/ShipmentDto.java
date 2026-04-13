package com.goodee.beedan.dto.order;

import com.goodee.beedan.common.constant.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
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