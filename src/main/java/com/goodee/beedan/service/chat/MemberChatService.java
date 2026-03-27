package com.goodee.beedan.service.chat;

import com.goodee.beedan.common.constant.ChatRoomStatus;
import com.goodee.beedan.dto.chat.ChatRoomOpenResultDto;
import com.goodee.beedan.dto.chat.MemberChatRoomListDto;
import com.goodee.beedan.dto.chatbot.ChatbotTopLevelTopicDto;
import com.goodee.beedan.entity.ChatRoom;
import com.goodee.beedan.repository.chat.ChatMessageRepository;
import com.goodee.beedan.repository.chat.ChatRoomReadStatusRepository;
import com.goodee.beedan.repository.chat.ChatRoomRepository;
import com.goodee.beedan.service.chatbot.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberChatService {
    private static final String DEFAULT_CHAT_ROOM_TITLE = "기타 문의";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatbotService chatbotService;

    // 챗봇에서 상담사 연결 시 활성 채팅방이 있으면 기존 방 반환, 없으면 새 방 생성
    public ChatRoomOpenResultDto openChatRoomFromChatbot(Long topicId, Long memId) {
        List<ChatRoomStatus> activeStatuses = List.of(ChatRoomStatus.OPEN, ChatRoomStatus.ONGOING);

        ChatRoom activeRoom = chatRoomRepository
                .findFirstByMemIdAndChRoSttInOrderByChRoIdDesc(memId, activeStatuses)
                .orElse(null);

        if (activeRoom != null) {
            return mapToChatRoomOpenResultDto(activeRoom, true);
        }

        ChatbotTopLevelTopicDto topLevelTopic = chatbotService.getTopLevelTopic(topicId);
        String roomTitle = topLevelTopic != null ? topLevelTopic.getCbTpNm() : DEFAULT_CHAT_ROOM_TITLE;

        ChatRoom newRoom = ChatRoom.builder()
                .chRoTtl(roomTitle)
                .chRoStt(ChatRoomStatus.OPEN)
                .memId(memId)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);
        return mapToChatRoomOpenResultDto(savedRoom, false);
    }

    private ChatRoomOpenResultDto mapToChatRoomOpenResultDto(ChatRoom chatRoom, boolean existingRoom) {
        return ChatRoomOpenResultDto.builder()
                .chRoId(chatRoom.getChRoId())
                .chRoTtl(chatRoom.getChRoTtl())
                .chRoStt(chatRoom.getChRoStt())
                .existingRoom(existingRoom)
                .build();
    }

    private List<MemberChatRoomListDto> getMemberChatRooms(Long memId) {
        return null;
    }
}
