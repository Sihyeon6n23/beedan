package com.goodee.beedan.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoticeType {
    QUOTATION_APPROVE("견적 승인 완료", "요청하신 견적[%s]이 승인 처리되었습니다.", "/quote/detail?id=%s"),
    QUOTATION_REJECT("견적 요청 거절됨", "요청하신 견적[%s]이 반려되었습니다.", "/quote/detail?id=%s"),

    INQUIRY_ANSWER("문의 답변 완료", "요청하신 문의[%s] 답변이 완료되었습니다.", "/inquiry/detail?id=%s"),

    ORDER_APPROVE("주문 완료", "요청하신 주문[%s]이 승인 처리되었습니다.", "/order/detail?id=%s"),

    SHIPMENT_START("배송 시작", "상품[%s]의 배송이 시작되었습니다.", "/order/list"),
    SHIPMENT_END("배송 완료", "상품[%s]의 배송이 완료되었습니다.", "/order/list"),

    PASSWORD_RESET("비밀번호 재설정 안내", "비밀번호 초기화를 위한 인증 번호[%s]가 발급되었습니다.", "/");

    private final String defaultTitle;
    private final String contentTemplate;
    private final String urlTemplate;

    public String generateContent(String detail) {
        return String.format(this.contentTemplate, detail); // ex) 요청하신 견적[여름 상의 외 3개]이 승인 처리되었습니다. 해당 테이블의 select를 사용해서 내부에 넣으면 좋을 듯
    }
    public String generateUrl(Object targetId) {return String.format(this.urlTemplate, targetId);}
}
