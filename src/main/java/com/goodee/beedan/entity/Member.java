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

    @Enumerated(EnumType.STRING)
    private MemberAuthority memAut;

    private String memNm;
    private String memMbPhn;
    private String memCi;
    private String memEml;
    private String memBizNo;
    private LocalDateTime memBizCreDt;
    private String memBizTtl;

    private String memPosCd;

    private String memBizAdr;
    private String memBizDtAdr;
    private String memCeoNm;
    private String memCeoPhn;
    private String memCmpTel;

    // 🎯 SQL은 BIGINT이므로 Long으로 변경 권장
    private Long memLgnTr;

    private LocalDateTime memLocDt;

    // 🎯 SQL이 ENUM이므로 엔티티도 타입을 맞추거나 String 유지
    private String memStt;

    @LastModifiedBy
    private Long memUpdId; // 🎯 SQL은 BIGINT이므로 Long으로 변경

    @LastModifiedDate
    private LocalDateTime memUpdDt;
    @CreatedDate
    private LocalDateTime memCreDt;
}