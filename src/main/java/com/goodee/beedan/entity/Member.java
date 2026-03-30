package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.MemberAuthority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // SQL의 AUTO_INCREMENT와 매핑
    private Long memId;

    @Column(nullable = false, unique = true)
    private String memLgnId;

    @Column(nullable = false)
    private String memLgnPw;

    private String memEml;

    @Enumerated(EnumType.STRING)
    private MemberAuthority memAut;

    private String memNm;
    private String memMbPhn;
    private String memCi;

    private String memBizNo;
    private LocalDateTime memBizCreDt;
    private String memBizTtl;
    private String memCeoNm;

    private String memPosCd;
    private String memBizAdr;
    private String memBizDtAdr;
    private String memCeoPhn;
    private String memCmpTel;

    private Long memLgnTr;
    private LocalDateTime memLocDt;
    private String memStt;

    @LastModifiedBy
    private Long memUpdId;

    @LastModifiedDate
    private LocalDateTime memUpdDt;
    @CreatedDate
    private LocalDateTime memCreDt;

    private LocalDateTime memUpdPwDt;
}