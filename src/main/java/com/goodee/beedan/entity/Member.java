package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.MemberAuthority;
import jakarta.persistence.*;
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
    private Long memId;
    private String memLgnId;
    private String memLgnPw;
    @Enumerated(EnumType.STRING) // 0323 임욱: Enum 매핑을 위해 추가
    private MemberAuthority memAut;
    private String memNm;
    private String memMbPhn;
    private String memCi;
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
