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
@Table(name = "NEGOTIATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Negotiation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ngId;
    private String ngNm;    //협상명
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime ngCreDt;  // 협상 시작 시간
    private LocalDateTime ngEndDt;  // 협상 종료 시간
    private Long memId; // 사용자 아이디

    @Builder
    public Negotiation(String name, Long memId){
        this.ngNm = name;
        this.memId = memId;
    }

    public void close() {
        this.ngEndDt = LocalDateTime.now();
    }
    public boolean isOngoing() {
        return this.ngEndDt == null;
    }
}
