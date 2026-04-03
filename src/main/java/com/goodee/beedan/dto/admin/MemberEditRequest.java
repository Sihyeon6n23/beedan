package com.goodee.beedan.dto.admin;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEditRequest {

    private Long id;                // hidden field: 수정 대상 식별

    // 01. 사업자 및 대표자 정보 (대표자 연락처만 수정 가능)
    private String phone;           // th:field="*{phone}" (대표자 연락처)

    // 02. 담당자 정보
    private String managerName;      // th:field="*{managerName}"
    private String managerPhone;     // th:field="*{managerPhone}"
    private String email;            // th:field="*{email}"

    // 03. 회사 주소
    private String postCode;        // th:field="*{postCode}"
    private String address;         // th:field="*{address}" (기본 주소)
    private String addressDetail;   // th:field="*{addressDetail}" (상세 주소)

    // 04. 회원정보 이용 이력
    private String status;          // th:field="*{status}"
}