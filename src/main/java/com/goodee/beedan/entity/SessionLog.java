package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slId;

    private String slSsId;           // HttpSession ID
    private Long slMemId;            // 회원 ID (비로그인이면 null)
    private LocalDateTime slStDt;    // 세션 시작
    private LocalDateTime slEnDt;    // 세션 종료
    private Long slDuSec;            // 체류 시간 (초)

    @Builder
    public SessionLog(String sessionId, Long memId, LocalDateTime startDt) {
        this.slSsId = sessionId;
        this.slMemId = memId;
        this.slStDt = startDt;
    }

    public void endSession(LocalDateTime endDt) {
        this.slEnDt = endDt;
        if (this.slStDt != null && endDt != null) {
            this.slDuSec = java.time.Duration.between(this.slStDt, endDt).getSeconds();
        }
    }

    public void setMemId(Long memId) {
        this.slMemId = memId;
    }
}
