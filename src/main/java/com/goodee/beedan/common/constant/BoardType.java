package com.goodee.beedan.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BoardType {
    // 1. 공지사항: 고정글(Fix) 사용, 문의상태(Status) 미사용
    NOTICE("공지사항", true, false, true),

    // 2. 문의게시판: 고정글 미사용, 문의상태(Status) 사용
    INQUIRY("문의게시판", false, true, true),

    // 3. 문의답변: 문의게시판의 연장선이므로 상태값은 공유하나 목록에는 보통 안 나옴
    INQUIRY_ANSWER("문의답변", false, true, true),

    // 4. 자유게시판: 모두 미사용
    FREE("자유게시판", false, false, false),

    // 5. 사업자승인메뉴: 모두 미사용
    APPROVE("사업자인증", false, false, false);

    private final String description;
    private final boolean useFixed;  // 고정글 기능 활성화 여부
    private final boolean useStatus; // 접수/처리중/완료 등 상태값 사용 여부
    private final boolean fileUpload;
}