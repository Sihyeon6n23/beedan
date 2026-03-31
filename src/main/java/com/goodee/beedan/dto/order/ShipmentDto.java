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
    private Long shi_id;
    private Long shi_tra_no;
    private String shi_ca_cd;
    private ShipmentStatus shi_stt;
    private LocalDateTime shi_base_cre_dt;
    private LocalDateTime shi_div_dt;
}
