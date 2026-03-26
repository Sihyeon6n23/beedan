package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Receiver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rcId;
    private String rcNm;
    private String rcPhn;
    private String rcMsg;
    private String rcAdr;
    private String rcAdrDt;
    @CreatedDate
    private LocalDateTime rcCreDt;
    @LastModifiedDate
    private LocalDateTime rcUpdDt;
    private Boolean rcDelYn;

    @ManyToOne
    @JoinColumn(name = "mem_id")
    private Member member;
}