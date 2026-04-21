package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QuoteSubmitCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qscId;

    private Boolean qscRqYn;       // 필수 여부

    @Column(columnDefinition = "TEXT")
    private String qscDes;         // 체크 항목 텍스트

    private String qscKey;         // 알림 설정용 식별키

    private Boolean qscDfltYn;     // 기본 체크 여부

    private Integer qscSort;       // 정렬 순서

    private Boolean qscYn;         // 활성화 여부

    @CreatedDate
    private LocalDateTime qscCrDt;

    @LastModifiedDate
    private LocalDateTime qscUpDt;

    @Builder
    public QuoteSubmitCheck(Boolean qscRqYn, String qscDes, String qscKey,
                            Boolean qscDfltYn, Integer qscSort) {
        this.qscRqYn = qscRqYn;
        this.qscDes = qscDes;
        this.qscKey = qscKey;
        this.qscDfltYn = qscDfltYn;
        this.qscSort = qscSort;
        this.qscYn = true;
    }

    public void update(Boolean requiredYn, String description, String key,
                       Boolean defaultYn, Integer sort) {
        this.qscRqYn = requiredYn;
        this.qscDes = description;
        this.qscKey = key;
        this.qscDfltYn = defaultYn;
        this.qscSort = sort;
    }

    public void deactivate() {
        this.qscYn = false;
    }
}
