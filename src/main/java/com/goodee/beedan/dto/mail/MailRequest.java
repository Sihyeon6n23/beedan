package com.goodee.beedan.dto.mail;

import com.goodee.beedan.common.constant.NotificationType;

public sealed interface MailRequest permits PasswordResetMailRequest, BizValidationRejectMailRequest {
    String getEmail();
    NotificationType getType();
    Object getTargetId();

}
