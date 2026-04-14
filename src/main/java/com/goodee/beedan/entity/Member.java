package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.MemberBizStatus;
import com.goodee.beedan.common.constant.MemberStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
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

    private String memBizStt;

    public void bizApprove() {
        if (this.memStt.equals(MemberBizStatus.APPROVAL.toString())) {
            throw new IllegalArgumentException("이미 승인이 완료된 계정입니다.");
        }
        this.memBizStt = MemberBizStatus.APPROVAL.toString();
    }

    public void bizReject() {
        if (this.memStt.equals(MemberBizStatus.REJECT.toString())) {
            throw new IllegalArgumentException("이미 반려된 계정입니다.");
        }
        this.memBizStt = MemberBizStatus.REJECT.toString();
    }

    public void withdraw() {
        this.memLgnId = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();

        this.memLgnPw = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();

        this.memEml = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();
        this.memNm = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();
        this.memMbPhn = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();
        // Ci값은 암호화해서 별도 저장?
        // this.memCi = ;

        this.memBizNo = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();
        this.memBizTtl = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();
        this.memCeoNm = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();

        this.memPosCd = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();
        this.memBizAdr = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();
        this.memBizDtAdr = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();
        this.memCeoPhn = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();
        this.memCmpTel = MemberStatus.WITHDRAWN.toString() + UUID.randomUUID().toString();
        this.memStt = MemberStatus.WITHDRAWN.toString();
    }


    public boolean isAdmin() {
        return this.memAut == MemberAuthority.ADMIN || this.memAut == MemberAuthority.ROOT;
    }

    public void validateAdmin() {
        if (!isAdmin()) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }
    }

}