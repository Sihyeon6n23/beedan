package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Data @Builder @RequiredArgsConstructor @AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_id")
    private Long notiId;
    private String notiCon;
    private String notiTtl;
    @CreatedDate
    private LocalDateTime notiCreDt;
    @LastModifiedDate
    private LocalDateTime notiUpdDt;
    private Boolean notiReaYn;
    private Boolean notiDelYn;
    private Long notiUpdMemId;
    private String notiRef;

    @ManyToOne
    @JoinColumn(name = "mem_id")
    private Member member;
}
