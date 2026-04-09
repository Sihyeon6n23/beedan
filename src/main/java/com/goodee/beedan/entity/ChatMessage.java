package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.ChatMessageSenderType;
import com.goodee.beedan.common.constant.ChatMessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ch_ms_id")
    private Long chMsId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ch_ms_sen_ty", nullable = false, length = 20)
    private ChatMessageSenderType chMsSenTy;

    @Enumerated(EnumType.STRING)
    @Column(name = "ch_ms_tp", nullable = false)
    private ChatMessageType chMsTp;

    @Lob
    @Column(name = "ch_ms_con", nullable = false)
    private String chMsCon;

    @Column(name = "ch_ms_lnk_url")
    private String chMsLnkUrl;

    @Column(name = "ch_ms_lnk_ttl")
    private String chMsLnkTtl;

    @CreatedDate
    @Column(name = "ch_ms_cre_dt", nullable = false, updatable = false)
    private LocalDateTime chMsCreDt;

    @Column(name = "ch_ro_id", nullable = false)
    private Long chRoId;

    @Column(name = "mem_id", nullable = false)
    private Long memId;
}
