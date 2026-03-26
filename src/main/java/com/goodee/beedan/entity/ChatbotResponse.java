package com.goodee.beedan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cbResId;
    private String cbResMaTpNm;
    private String cbResSubTpNm;
    private String cbResTtl;
    private String cbResCon;
    private boolean cbResBtnYn;
    private boolean cbResActYn;
    @CreatedDate
    private LocalDateTime cbResCreDt;
    @LastModifiedDate
    private LocalDateTime cbResUpdDt;
}
