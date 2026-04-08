package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.QuoteStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Random;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QuoteBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quId;
    private String quCd; // 견적 코드
    private Long ngId;  // 협상 아이디
    private Long quSid; // 송신자 아이디
    private Long quRid; // 수신자 아이디
    @Enumerated(EnumType.STRING)
    private QuoteStatus quStt;  // 견적 상태
    private LocalDateTime quExpDt;  // 만료 시간
    private Boolean quAdOpYn;   // 운영자 열람 여부
    private Boolean quUsOpYn;   // 사용자 열람 여부
    private String quCon;   // 답변 내용
    @CreatedDate
    private LocalDateTime quCreDt;
    @LastModifiedDate
    private LocalDateTime quUpdDt;

    @Builder
    public QuoteBase(
            Long negoId,
            Long senderId,
            Long receiverId
    ){
        this.quCd = "QU" + (System.currentTimeMillis() % 10000);
        this.ngId = negoId;
        this.quSid = senderId;
        this.quRid = receiverId;
        this.quStt = null;
        this.quAdOpYn = false;
        this.quUsOpYn = false;
    }

    public void setQuCd(String quCd) {
        this.quCd = quCd;
    }

    public void tempSave() {
        this.quStt = QuoteStatus.TEMP_SAVE;
    }

    public void submit(){
        this.quStt = QuoteStatus.SUBMITTED;
    }

    public void approve(){
        this.quStt = QuoteStatus.APPROVED;
    }

    public void reject(String reason) {
        this.quStt = QuoteStatus.REJECTED;
        this.quCon = reason;
    }

    public void expire() {
        this.quStt = QuoteStatus.EXPIRED;
    }

    public void paid() {
        this.quStt = QuoteStatus.PAID;
    }

    public void adminOpened() {
        this.quAdOpYn = true;
    }

    public void userOpened() {
        this.quUsOpYn = true;
    }

    public boolean isExpired() {
        if (this.quExpDt == null) return false;
        return LocalDateTime.now().isAfter(this.quExpDt);
    }
    public boolean isEditable() {
        return this.quStt == null
                || QuoteStatus.TEMP_SAVE.equals(this.quStt)
                || QuoteStatus.REJECTED.equals(this.quStt);
    }









}
