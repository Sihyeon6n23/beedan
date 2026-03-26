package com.goodee.beedan.dto.notice;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NoticeDto {
    private Long notiId;
    private String notiCon;
    private String notiTtl;
    private LocalDateTime notiCreAt;
    private LocalDateTime notiUptDt;
    private Boolean notiReaYn;
    private Long memberId;
}
