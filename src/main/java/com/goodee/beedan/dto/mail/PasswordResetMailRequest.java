package com.goodee.beedan.dto.mail;

import com.goodee.beedan.common.constant.NotificationType;
import org.springframework.beans.factory.annotation.Value;

public final record PasswordResetMailRequest(
        String email,
        Object targetId,
        String token,
        String baseUrl) implements MailRequest {

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.PASSWORD_RESET;
    }

    @Override
    public Object getTargetId() {
        return targetId;
    }

    public String getFullUrl() {
        return baseUrl + NotificationType.PASSWORD_RESET.generateUrl(token);
    }
}
