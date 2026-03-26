package com.goodee.beedan.dto.notification;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDto {
    private Long notiId;
    private String notiCon;
    private String notiTtl;
    private LocalDateTime notiCreAt;
    private LocalDateTime notiUptDt;
    private Boolean notiReaYn;
    private Long memberId;
}
