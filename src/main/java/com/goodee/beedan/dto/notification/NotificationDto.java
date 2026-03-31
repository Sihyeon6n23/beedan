package com.goodee.beedan.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder
@RequiredArgsConstructor @AllArgsConstructor
public class NotificationDto {
    private Long notiId;
    private String notiCon;
    private String notiTtl;
    private LocalDateTime notiCreAt;
    private LocalDateTime notiUptDt;
    private Boolean notiReaYn;
    private Long memberId;
}
