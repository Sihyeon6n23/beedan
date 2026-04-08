package com.goodee.beedan.service.chat;

import com.goodee.beedan.common.constant.ChatRoomCloseReason;
import com.goodee.beedan.common.constant.ChatRoomStatus;
import com.goodee.beedan.devUtils.AppDateTime;
import com.goodee.beedan.entity.ChatRoom;
import com.goodee.beedan.repository.chat.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSchedulerService {
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public int closeInactiveChatRooms() {
//        LocalDateTime now = LocalDateTime.now();
        // 테스트용 dev 시간
        LocalDateTime now = AppDateTime.now();
        LocalDateTime cutoff = now.minusDays(3);

        List<ChatRoomStatus> activeStatuses = List.of(ChatRoomStatus.OPEN, ChatRoomStatus.ONGOING);
        List<ChatRoom> targetRooms = chatRoomRepository.findByChRoSttInAndChRoLastMsDtBefore(activeStatuses, cutoff);

        for (ChatRoom chatRoom : targetRooms) {
            chatRoom.setChRoStt(ChatRoomStatus.CLOSED);
            chatRoom.setChRoClsRsn(ChatRoomCloseReason.AUTO);
            chatRoom.setChRoClsDt(now);
        }

        return targetRooms.size();
    }
}
