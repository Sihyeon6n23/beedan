package com.goodee.beedan.repository.chat;

import com.goodee.beedan.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findFirstByChRoIdOrderByChMsCreDtDesc(Long chRoId);
}
