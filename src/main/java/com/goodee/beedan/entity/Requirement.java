package com.goodee.beedan.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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
    public String reqTtl;
    public String reqCon;
    public String reqRef;
    public String reqPr;
    public String reqStt;
    public Boolean reqRepYn;
    public Boolean reqPerYn;
    public LocalDateTime reqCreDt;
    public LocalDateTime reqUpdDt;
    public Boolean reqDelYn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id")
    public Member member;

}
