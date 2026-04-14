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

    // 사용자 채팅 목록 전용 projection
    // chat_room + 마지막 chat_message + chat_room_read_status를 한 번에 조회한 결과를
    // 사용자 채팅 목록 화면에 필요한 필드만 가볍게 받아오기 위한 repository 전용 projection 인터페이스
    interface MemberChatRoomListProjection {
        Long getChRoId();
        String getChRoTtl();
        String getChRoStt();
        LocalDateTime getChRoCreDt();
        String getChRoClsRsn();
        String getLastMessageContent();
        String getLastMessageType();
        java.time.LocalDateTime getLastMessageCreatedAt();
        Boolean getUnread();
    }

    // --- 사용자

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

    // 채팅 목록용 데이터 (채팅방 정보 + 마지막 메시지 + 마지막 메시지 시간 + 읽음 여부를 한번에 조회)
    @Query(value = """
          SELECT
              cr.ch_ro_id AS chRoId,
              cr.ch_ro_ttl AS chRoTtl,
              cr.ch_ro_stt AS chRoStt,
              cr.ch_ro_cre_dt AS chRoCreDt,
              cr.ch_ro_cls_rsn AS chRoClsRsn,
              lm.ch_ms_con AS lastMessageContent,
              lm.ch_ms_tp AS lastMessageType,
              lm.ch_ms_cre_dt AS lastMessageCreatedAt,
              COALESCE(rs.ch_ro_re_st_unr_yn, false) AS unread
          FROM chat_room cr
          LEFT JOIN chat_room_read_status rs
              ON rs.ch_ro_id = cr.ch_ro_id
             AND rs.mem_id = :memId
          LEFT JOIN chat_message lm
              ON lm.ch_ms_id = (
                  SELECT cm.ch_ms_id
                  FROM chat_message cm
                  WHERE cm.ch_ro_id = cr.ch_ro_id
                  ORDER BY cm.ch_ms_cre_dt DESC, cm.ch_ms_id DESC
                  LIMIT 1
              )
          WHERE cr.mem_id = :memId
          ORDER BY
              CASE
                  WHEN cr.ch_ro_stt IN ('OPEN', 'ONGOING') THEN 0
                  ELSE 1
              END,
              cr.ch_ro_last_ms_dt DESC,
              cr.ch_ro_cre_dt DESC
          """, nativeQuery = true)
    List<MemberChatRoomListProjection> findMemberChatRoomListSummaries(@Param("memId") Long memId);

    Optional<ChatRoom> findByChRoIdAndMemId(Long chRoId, Long memId);

    // --- 관리자

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

    // 기간 내 토픽별 문의 건수
    @Query("SELECT cr.chRoTtl, COUNT(cr) FROM ChatRoom cr WHERE cr.chRoCreDt BETWEEN :from AND :to GROUP BY cr.chRoTtl ORDER BY COUNT(cr) DESC")
    List<Object[]> countGroupByTopic(@org.springframework.data.repository.query.Param("from") LocalDateTime from,
                                     @org.springframework.data.repository.query.Param("to") LocalDateTime to);
}
