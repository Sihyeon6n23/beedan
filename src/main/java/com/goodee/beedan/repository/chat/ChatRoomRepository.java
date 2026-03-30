package com.goodee.beedan.repository.chat;

import com.goodee.beedan.common.constant.ChatRoomStatus;
import com.goodee.beedan.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findFirstByMemIdAndChRoSttInOrderByChRoIdDesc(Long memId, Collection<ChatRoomStatus> statuses);
    List<ChatRoom> findByMemIdOrderByChRoLastMsDtDescChRoCreDtDesc(Long memId);
    Optional<ChatRoom> findByChRoIdAndMemId(Long chRoId, Long memId);

    // 전체 목록
    List<ChatRoom> findAllByOrderByChRoLastMsDtDescChRoCreDtDesc();
    // 전체 목록 + 페이징
    @Query("""
            SELECT c
            FROM ChatRoom c
            ORDER BY
                CASE
                    WHEN c.chRoStt = 'OPEN' THEN 0
                    WHEN c.chRoStt = 'ONGOING' THEN 1
                    ELSE 2
                END,
                c.chRoLastMsDt DESC,
                c.chRoCreDt DESC
            """)
    Page<ChatRoom> findAllByPriorityOrder(Pageable pageable);
    // 전체 목록 + 페이징 + 상태 필터
    Page<ChatRoom> findByChRoSttOrderByChRoLastMsDtDescChRoCreDtDesc(ChatRoomStatus chRoStt, Pageable pageable);
    // 내 담당 목록 + 페이징
    @Query("""
            SELECT c
            FROM ChatRoom c
            WHERE c.memAdId = :memAdId
            ORDER BY
                CASE
                    WHEN c.chRoStt = 'OPEN' THEN 0
                    WHEN c.chRoStt = 'ONGOING' THEN 1
                    ELSE 2
                END,
                c.chRoLastMsDt DESC,
                c.chRoCreDt DESC
            """)
    Page<ChatRoom> findByMemAdIdPriorityOrder(Long memAdId, Pageable pageable);
    // 내 담당 목록 + 페이징 + 상태 필터
    Page<ChatRoom> findByChRoSttAndMemAdIdOrderByChRoLastMsDtDescChRoCreDtDesc(ChatRoomStatus chRoStt, Long memAdId, Pageable pageable);
}
