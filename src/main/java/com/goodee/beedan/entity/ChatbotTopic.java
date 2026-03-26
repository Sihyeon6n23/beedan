package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_topic")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cb_tp_id")
    private Long cbTpId;

    @Column(name = "cb_tp_nm", nullable = false, length = 100)
    private String cbTpNm;

    @Column(name = "cb_tp_lvl", nullable = false)
    private Integer cbTpLvl;

    @Column(name = "cb_tp_ord", nullable = false)
    private Integer cbTpOrd;

    @Column(name = "cb_tp_use_yn", nullable = false)
    private boolean cbTpUseYn;

    @CreatedDate
    @Column(name = "cb_tp_cre_dt", nullable = false, updatable = false)
    private LocalDateTime cbTpCreDt;

    @LastModifiedDate
    @Column(name = "cb_tp_upd_dt", nullable = false)
    private LocalDateTime cbTpUpdDt;

    // 부모 질의 id만 관리해서 1차/2차 계층을 단순하게 유지
    @Column(name = "cb_tp_prn_id")
    private Long cbTpPrnId;
}
