package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.ChatRoomCloseReason;
import com.goodee.beedan.common.constant.ChatRoomStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ch_ro_id")
    private Long chRoId;

    @Column(name = "ch_ro_ttl", nullable = false, length = 255)
    private String chRoTtl;

    @Enumerated(EnumType.STRING)
    @Column(name = "ch_ro_stt", nullable = false, length = 20)
    private ChatRoomStatus chRoStt;

    @CreatedDate
    @Column(name = "ch_ro_cre_dt", nullable = false, updatable = false)
    private LocalDateTime chRoCreDt;

    @Column(name = "ch_ro_asg_dt")
    private LocalDateTime chRoAsgDt;

    @Column(name = "ch_ro_cls_dt")
    private LocalDateTime chRoClsDt;

    @Enumerated(EnumType.STRING)
    @Column(name = "ch_ro_cls_rsn", length = 20)
    private ChatRoomCloseReason chRoClsRsn;

    @Column(name = "ch_ro_last_ms_dt")
    private LocalDateTime chRoLastMsDt;

    @Column(name = "mem_id", nullable = false)
    private Long memId;

    // 담당 관리자 id는 OPEN 에서는 비어 있고, 배정 시점부터 채워짐
    @Column(name = "mem_ad_id")
    private Long memAdId;
}
