package com.goodee.beedan.dto.order;

import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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