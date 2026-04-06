package com.goodee.beedan.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    QUOTATION_APPROVE("견적 승인 완료", "요청하신 견적이 승인 처리되었습니다.", "/quote/detail?id=%s"),
    QUOTATION_REJECT("견적 요청 거절됨", "요청하신 견적이 반려되었습니다.", "/quote/detail?id=%s"),

    INQUIRY_ANSWER_CREATE("문의 답변 등록", "문의에 대한 답변이 등록되었습니다.", "/inquiry/detail?id=%s"),
    INQUIRY_ANSWER_UPDATE("문의 답변 수정", "문의 답변 내용이 수정되었습니다.", "/inquiry/detail?id=%s"),

    ORDER_APPROVE("주문 승인", "요청하신 주문이 승인 처리되었습니다.", "/order/detail?id=%s"),
    ORDER_CHANGE("주문 변경", "요청하신 주문의 상태가 변경 되었습니다.", "/order/detail?id=%s"),
    ORDER_CANCEL("주문 취소", "요청하신 주문이 취소 처리되었습니다.", "/order/detail?id=%s"),

    SHIPMENT_START("배송 시작", "상품[%s]의 배송이 시작되었습니다.", "/order/list"),
    SHIPMENT_END("배송 완료", "상품[%s]의 배송이 완료되었습니다.", "/order/list"),

    REQUIREMENT_APPROVE("상품 요청 승인", "상품 요청이 승인 처리되었습니다.", "/require/detail?id=%s"),
    REQUIREMENT_REJECT("상품 요청 반려", "상품 요청이 반려되었습니다.", "/require/detail?id=%s"),
    REQUIREMENT_CHANGE("상품 요청 변경", "상품 요청 답변이 수정되었습니다.", "/require/detail?id=%s"),

    PASSWORD_RESET("비밀번호 재설정 안내", "비밀번호 초기화를 위한 인증 번호가 발급되었습니다.", "/");

    private final String defaultTitle;
    private final String contentTemplate;
    private final String urlTemplate;

    public String generateUrl(Object targetId) {return String.format(this.urlTemplate, targetId);}
}
