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
public class RequirementReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long reqRepId;
    public Long reqId;
    public Long memId;
    public String reqRepTtl;
    public String reqRepCon;
    public Boolean reqRepPerYn;
    public LocalDateTime reqRepCreDt;
    public LocalDateTime reqRepUpdDt;
}
