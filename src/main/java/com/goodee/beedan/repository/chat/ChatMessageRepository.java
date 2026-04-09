package com.goodee.beedan.repository.chat;

import com.goodee.beedan.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findFirstByChRoIdOrderByChMsCreDtDesc(Long chRoId);
    List<ChatMessage> findByChRoIdOrderByChMsCreDtAsc(Long chRoId);

    @Query("""
            SELECT cm
            FROM ChatMessage cm
            WHERE cm.chMsId IN (
                SELECT MAX(sub.chMsId)
                FROM ChatMessage sub
                WHERE sub.chRoId IN :chRoIds
                GROUP BY sub.chRoId
            )
            """)
    List<ChatMessage> findLatestMessagesByChRoIds(@Param("chRoIds") Collection<Long> chRoIds);
}
