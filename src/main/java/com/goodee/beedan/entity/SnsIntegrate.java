package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.SnsType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnsIntegrate {
    @Id
    private Long id;
    private SnsType snsTp;
    private String snsSeNo;
    private LocalDateTime snsConDt;
    private LocalDateTime snsUpdDt;
    private Boolean snsCanYn;
}
