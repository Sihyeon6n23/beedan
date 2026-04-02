package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QuoteSubmitCheckLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qsclId;

    private Long quId;

    private Long qscId;

    private Boolean qsclChkYn;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime qsclCrDt;

    @Builder
    public QuoteSubmitCheckLog(Long quId, Long qscId, Boolean qsclChkYn) {
        this.quId = quId;
        this.qscId = qscId;
        this.qsclChkYn = qsclChkYn;
    }
}
