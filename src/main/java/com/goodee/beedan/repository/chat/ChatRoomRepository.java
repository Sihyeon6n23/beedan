package com.goodee.beedan.repository.chat;

import com.goodee.beedan.common.constant.ChatRoomStatus;
import com.goodee.beedan.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findFirstByMemIdAndChRoSttInOrderByChRoIdDesc(Long memId, Collection<ChatRoomStatus> statuses);
    List<ChatRoom> findByMemIdOrderByChRoLastMsDtDescChRoCreDtDesc(Long memId);
    Optional<ChatRoom> findByChRoIdAndMemId(Long chRoId, Long memId);
}
