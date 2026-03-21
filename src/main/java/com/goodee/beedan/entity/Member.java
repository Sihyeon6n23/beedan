package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.MemberAuthority;
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
public class Member {
    @Id
    private Long id;
    private Long memId;
    private String memLgnId;
    private String memLgnPw;
    private MemberAuthority memAut;
    private String memNm;
    private String memMbPhn;
    private String memEml;
    private String memBizNo;
    private String memBizTtl;
    private String memBizAdr;
    private String memCeoNm;
    private String memCeoPhn;
    private String memCmpTel;
    private String memLgnTr;
    private LocalDateTime memLocDt;
    private String memStt;
    private String memUpdId;
    private LocalDateTime memUpdDt;
}
