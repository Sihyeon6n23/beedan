package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_room_read_status",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_room_read_status_mem_room", columnNames = {"mem_id", "ch_ro_id"})
        }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomReadStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ch_ro_re_st_id")
    private Long chRoReStId;

    @LastModifiedDate
    @Column(name = "ch_ro_re_st_last_dt")
    private LocalDateTime chRoReStLastDt;

    @Column(name = "ch_ro_re_st_unr_yn", nullable = false)
    private boolean chRoReStUnrYn;

    @Column(name = "ch_ro_id", nullable = false)
    private Long chRoId;

    @Column(name = "mem_id", nullable = false)
    private Long memId;

    // 마지막으로 읽은 메시지 id를 저장해서 어디까지 읽었는지 판단
    @Column(name = "ch_ms_last_id")
    private Long chMsLastId;
}
