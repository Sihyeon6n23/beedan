package com.goodee.beedan.service.chat;

import com.goodee.beedan.common.constant.ChatRoomCloseReason;
import com.goodee.beedan.common.constant.ChatRoomStatus;
import com.goodee.beedan.devUtils.AppDateTime;
import com.goodee.beedan.dto.chat.ChatRoomStatusEventDto;
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
    private final ChatRealtimeService chatRealtimeService;

    @Transactional
    public int closeInactiveChatRooms(long inactiveHours) {
//        LocalDateTime now = LocalDateTime.now();
        // 테스트용 dev 시간
        LocalDateTime now = AppDateTime.now();
        LocalDateTime cutoff = now.minusHours(inactiveHours);

        List<ChatRoomStatus> activeStatuses = List.of(ChatRoomStatus.OPEN, ChatRoomStatus.ONGOING);
        List<ChatRoom> targetRooms =
                chatRoomRepository.findByChRoSttInAndChRoLastMsDtBefore(activeStatuses, cutoff);

        for (ChatRoom chatRoom : targetRooms) {
            chatRoom.setChRoStt(ChatRoomStatus.CLOSED);
            chatRoom.setChRoClsRsn(ChatRoomCloseReason.AUTO);
            chatRoom.setChRoClsDt(now);

            // 자동 종료도 수동 종료와 동일하게 목록/상세에 즉시 반영
            chatRealtimeService.publishMemberSummary(chatRoom.getMemId());
            chatRealtimeService.publishAdminSummary();
            chatRealtimeService.publishRoomStatus(
                    chatRoom.getChRoId(),
                    ChatRoomStatusEventDto.builder()
                            .eventType("ROOM_STATUS")
                            .chRoId(chatRoom.getChRoId())
                            .chRoStt(chatRoom.getChRoStt())
                            .chRoClsRsn(chatRoom.getChRoClsRsn())
                            .build()
            );
        }

        return targetRooms.size();
    }
}
