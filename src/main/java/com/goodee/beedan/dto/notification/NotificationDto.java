package com.goodee.beedan.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime notiCreDt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime notiUptDt;
    private Boolean notiReaYn;
    private String notiRef;
}
