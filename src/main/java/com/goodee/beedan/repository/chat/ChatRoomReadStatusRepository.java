package com.goodee.beedan.repository.chat;

import com.goodee.beedan.entity.ChatRoomReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomReadStatusRepository extends JpaRepository<ChatRoomReadStatus, Long> {
    Optional<ChatRoomReadStatus> findByMemIdAndChRoId(Long memId, Long chRoId);
}
