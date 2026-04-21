package com.goodee.beedan.dto.mail;

import com.goodee.beedan.common.constant.NotificationType;

public final record BizValidationRejectMailRequest(
        String email) implements MailRequest {

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.BIZ_REJECT;
    }

    @Override
    public Object getTargetId() {
        return null;
    }
}
