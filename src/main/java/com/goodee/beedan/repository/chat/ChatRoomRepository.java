package com.goodee.beedan.repository.chat;

import com.goodee.beedan.common.constant.ChatRoomStatus;
import com.goodee.beedan.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 사용자
    Optional<ChatRoom> findFirstByMemIdAndChRoSttInOrderByChRoIdDesc(Long memId, Collection<ChatRoomStatus> statuses);

    List<ChatRoom> findByMemIdOrderByChRoLastMsDtDescChRoCreDtDesc(Long memId);
    @Query("""
      SELECT cr
      FROM ChatRoom cr
      WHERE cr.memId = :memId
      ORDER BY
          CASE
              WHEN cr.chRoStt IN ('OPEN', 'ONGOING') THEN 0
              ELSE 1
          END,
          cr.chRoLastMsDt DESC,
          cr.chRoCreDt DESC
  """)
    List<ChatRoom> findMemberChatRoomsByMemIdOrderByActiveFirst(@Param("memId") Long memId);

    Optional<ChatRoom> findByChRoIdAndMemId(Long chRoId, Long memId);

    // 관리자

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
    @Query(
            value = """
                    SELECT cr.*
                    FROM chat_room cr
                    JOIN member m ON m.mem_id = cr.mem_id
                    WHERE (:status = 'ALL' OR cr.ch_ro_stt = :status)
                      AND (:myAssignedOnly = false OR cr.mem_ad_id = :memAdId)
                      AND (
                          :keyword = ''
                          OR LOWER(COALESCE(m.mem_biz_ttl, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(COALESCE(m.mem_nm, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(COALESCE(cr.ch_ro_ttl, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                    ORDER BY
                        CASE
                            WHEN cr.ch_ro_stt = 'OPEN' THEN 0
                            WHEN cr.ch_ro_stt = 'ONGOING' THEN 1
                            ELSE 2
                        END,
                        cr.ch_ro_last_ms_dt DESC,
                        cr.ch_ro_cre_dt DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM chat_room cr
                    JOIN member m ON m.mem_id = cr.mem_id
                    WHERE (:status = 'ALL' OR cr.ch_ro_stt = :status)
                      AND (:myAssignedOnly = false OR cr.mem_ad_id = :memAdId)
                      AND (
                          :keyword = ''
                          OR LOWER(COALESCE(m.mem_biz_ttl, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(COALESCE(m.mem_nm, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(COALESCE(cr.ch_ro_ttl, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                    """,
            nativeQuery = true
    )
    Page<ChatRoom> searchAdminChatRooms(
            @Param("memAdId") Long memAdId,
            @Param("status") String status,
            @Param("myAssignedOnly") boolean myAssignedOnly,
            @Param("keyword") String keyword,
            Pageable pageable
    );
    // 채팅방 자동 종료 대상 조회
    List<ChatRoom> findByChRoSttInAndChRoLastMsDtBefore(Collection<ChatRoomStatus> chRoStts, LocalDateTime cutoff);
}
