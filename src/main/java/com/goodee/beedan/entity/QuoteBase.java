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

@Entity
@Table(name = "QU_BASE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QuoteBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quId;
    private Long ngId;  // 협상 아이디
    private Long quSid; // 송신자 아이디
    private Long quRid; // 수신자 아이디
    @Enumerated(EnumType.STRING)
    private QuoteStatus quStt;  // 견적 상태
    private LocalDateTime quExpDt;  // 만료 시간
    private Boolean quOpenYn;   // 열람 여부
    private String quCon;   // 답변 내용
    @CreatedDate
    private LocalDateTime quCreDt;
    @LastModifiedDate
    private LocalDateTime quUpdDt;

    @Builder
    public QuoteBase(
            Long negoId,
            Long senderId,
            Long receiverId,
            LocalDateTime desiredDate
    ){
        this.ngId = negoId;
        this.quSid = senderId;
        this.quRid = receiverId;
        this.quStt = QuoteStatus.TEMP_SAVE;
        this.quOpenYn = false;
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

    public void opened() {
        this.quOpenYn = true;
    }

    public boolean isExpired() {
        if (this.quExpDt == null) return false;
        return LocalDateTime.now().isAfter(this.quExpDt);
    }
    public boolean isEditable() {
        return QuoteStatus.TEMP_SAVE.equals(this.quStt);
    }









}
