package com.goodee.beedan.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotiDto {
    private Long notiId;
    private String notiCon;
    private String notiTtl;
    private LocalDateTime notiCreAt;
    private LocalDateTime notiUptDt;
    private Boolean notiReaYn;
    private Boolean notiDelYn;

    private Long memberId;
}
