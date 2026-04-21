package com.goodee.beedan.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Requirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long reqId;
    public Long memId;
    public String memNm;
    public String memBizTtl;
    public String reqTtl;
    public String reqCon;
    public String reqRef;
    public BigDecimal reqPr;
    public String reqCur;
    public String reqStt;
    public Boolean reqRepYn;
    public Boolean reqPerYn;
    public LocalDateTime reqCreDt;
    public LocalDateTime reqUpdDt;
    public Boolean reqDelYn;



}
