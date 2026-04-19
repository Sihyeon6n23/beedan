package com.goodee.beedan.dto.admin;

import com.goodee.beedan.entity.Member;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEditResponse {

    private Long id;                // memId
    private String loginId;         // memLgnId

    // [01] 사업자 및 대표자 정보 (Read-only 위주)
    private String companyName;      // memBizTtl (법인명)
    private String businessNumber;   // memBizNo (사업자번호)
    private LocalDateTime bizCreatedAt; // memBizCreDt (개업년월일 - 추가)
    private String representativeName; // memCeoNm (대표자명)
    private String phone;            // memCeoPhn (대표자 연락처 - Edit 가능)

    // [02] 담당자 정보 (Edit)
    private String managerName;      // memNm (담당자 성함/회원 이름 - 추가)
    private String managerPhone;     // memMbPhn (담당자 연락처)
    private String email;            // memEml (담당자 이메일)

    // [03] 회사 주소 (Edit)
    private String postCode;
    private String address;          // memBizAdr + memBizDtAdr 결합
    private String addressDetail;

    // [04] 회원정보 이용 이력 (Read-only & Status Edit)
    private String status;           // memStt (계정 상태)
    private String bizStatus;        // memBizStt (사업자 인증여부)
    private LocalDateTime createdAt; // memCreDt (가입일)
    private LocalDateTime updatedAt; // memUpdDt (최종 수정일)

    /**
     * Entity -> DTO 변환 메서드
     */
    public static MemberEditResponse fromEntity(Member member) {
        return MemberEditResponse.builder()
                .id(member.getMemId())
                .loginId(member.getMemLgnId())

                // 01. 사업자 정보 매핑
                .companyName(member.getMemBizTtl())
                .businessNumber(member.getMemBizNo())
                .bizCreatedAt(member.getMemBizCreDt()) // 개업년월일 추가
                .representativeName(member.getMemCeoNm())
                .phone(member.getMemCeoPhn()) // 대표 연락처

                // 02. 담당자 정보 매핑 (회원 이름 memNm 사용)
                .managerName(member.getMemNm())
                .managerPhone(member.getMemMbPhn())
                .email(member.getMemEml())

                // 03. 주소 매핑
                .postCode(member.getMemPosCd())
                .address(member.getMemBizAdr())
                .addressDetail(member.getMemBizDtAdr())

                // 04. 이력 매핑
                .status(member.getMemStt())
                .bizStatus(member.getMemBizStt())
                .createdAt(member.getMemCreDt())
                .updatedAt(member.getMemUpdDt())
                .build();
    }
}