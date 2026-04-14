package com.goodee.beedan.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    QUOTATION_APPROVE("견적 승인 완료", "요청하신 견적이 승인 처리되었습니다.", Path.QUOTE_DETAIL),
    QUOTATION_REJECT("견적 반려", "요청하신 견적이 반려되었습니다.", Path.QUOTE_DETAIL),
    QUOTATION_REVIEW("견적 확인", "큐레이터가 견적 요청서를 확인하였습니다.", Path.QUOTE_DETAIL),
    QUOTATION_REPLY("견적 회신", "견적서가 회신되었습니다.", Path.QUOTE_DETAIL),
    PAYMENT_COMPLETE("결제 완료", "결제가 정상적으로 완료되었습니다.", Path.QUOTE_DETAIL),
    SHIPMENT_UPDATE("배송 상태 변경", "배송 상태가 변경되었습니다.", Path.ORDER_DETAIL),

    INQUIRY_ANSWER_CREATE("문의 답변 등록", "문의에 대한 답변이 등록되었습니다.", Path.INQUIRY_DETAIL),
    INQUIRY_ANSWER_UPDATE("문의 답변 수정", "문의 답변 내용이 수정되었습니다.", Path.INQUIRY_DETAIL ),

    ORDER_APPROVE("주문 승인", "요청하신 주문이 승인 처리되었습니다.", Path.ORDER_DETAIL),
    ORDER_CHANGE("주문 변경", "요청하신 주문의 상태가 변경 되었습니다.", Path.ORDER_DETAIL),
    ORDER_CANCEL("주문 취소", "요청하신 주문이 취소 처리되었습니다.", Path.ORDER_DETAIL),

    DELIVERING_START("배송 시작", "국내 배송이 시작되었습니다.", Path.ORDER_DETAIL),
    DELIVERING_END("배송 완료", "상품 배송이 완료되었습니다.", Path.ORDER_DETAIL),

    REQUIREMENT_APPROVE("상품 요청 승인", "상품 요청이 승인 처리되었습니다.", Path.REQUIRE_DETAIL),
    REQUIREMENT_REJECT("상품 요청 반려", "상품 요청이 반려되었습니다.", Path.REQUIRE_DETAIL),
    REQUIREMENT_CHANGE("상품 요청 변경", "상품 요청 답변이 수정되었습니다.", Path.REQUIRE_DETAIL),

    PASSWORD_RESET("비밀번호 재설정 안내", "비밀번호 초기화를 위한 인증 번호가 발급되었습니다.", "/auth/passwd/change?token_id=%s");

    private final String defaultTitle;
    private final String message;
    private final String urlTemplate;

    public String generateUrl(Object targetId) {return String.format(this.urlTemplate, targetId);}

    private static class Path {
        private static final String QUOTE_DETAIL = "/quote/detail?quId=%s";
        private static final String ORDER_DETAIL = "/order/detail?id=%s";
        private static final String REQUIRE_DETAIL = "/require/detail?id=%s";
        private static final String INQUIRY_DETAIL = "/inquiry/detail?id=%s";
    }

}