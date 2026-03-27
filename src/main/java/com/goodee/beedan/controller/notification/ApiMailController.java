package com.goodee.beedan.controller.notification;

import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class ApiMailController {
    private final MailService mailService;

    @PostMapping("/mail")
    public ResponseEntity<String> testMail(@RequestParam String email) {
        // 테스트용 임시 데이터 세팅
        // 예: 주문 알림(ORDER)이나 배송 알림 등 기존에 정의하신 NotificationType을 사용하세요.
        mailService.sendMail(email, NotificationType.QUOTATION_APPROVE, "테스트 견적 승인", 1L);

        return ResponseEntity.ok("메일 발송 요청 성공! 네이버 메일함을 확인하세요.");
    }

}
