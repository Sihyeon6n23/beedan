package com.goodee.beedan.service.mail;

import com.goodee.beedan.dto.mail.MailRequest;
import com.goodee.beedan.dto.mail.PasswordResetMailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailNotificationService {
    private final MailService mailService; // 실제 @Async 전송 로직이 있는 서비스

    /**
     * 비즈니스 로직에서 이 메서드를 호출합니다.
     */
    public void sendNotification(MailRequest request) {

        // 1. 리퀘스트 타입에 따른 메일 내용 구성 (Switch문 활용)
        String subject = switch (request) {
            case PasswordResetMailRequest p -> "[BEEDAN] 비밀번호 재설정 안내";
        };

        String content = switch (request) {
            case PasswordResetMailRequest p -> buildPasswordResetHtml(p);
        };

        // 2. 실제 전송 서비스 호출 (비동기 실행)
        mailService.sendRowLevelMail(request.getEmail(), subject, content, request);
    }

    private String buildPasswordResetHtml(PasswordResetMailRequest request) {
        log.info("{}", request.getFullUrl());
        return String.format(
                """
            <div style="font-family: sans-serif; line-height: 1.6;">
                <h1 style="color: #333;">비밀번호 재설정</h1>
                <p>회원님, 안녕하세요.</p>
                <p>비밀번호 재설정을 위해 아래 버튼을 클릭해 주세요. 링크는 15분간 유효합니다.</p>
                <div style="margin: 30px 0;">
                    <a href="%s"
                       style="background-color: #007bff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        비밀번호 변경하기
                    </a>
                </div>
                <p style="font-size: 0.8em; color: #666;">만약 본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.</p>
            </div>
            """,
                request.getFullUrl()
        );
    }
}
