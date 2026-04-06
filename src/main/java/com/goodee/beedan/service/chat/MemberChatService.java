package com.goodee.beedan.service.chat;

import com.goodee.beedan.common.constant.ChatMessageSenderType;
import com.goodee.beedan.common.constant.ChatRoomCloseReason;
import com.goodee.beedan.common.constant.ChatRoomStatus;
import com.goodee.beedan.dto.chat.ChatRoomOpenResultDto;
import com.goodee.beedan.dto.chat.MemberChatMessageDto;
import com.goodee.beedan.dto.chat.MemberChatRoomDetailDto;
import com.goodee.beedan.dto.chat.MemberChatRoomListDto;
import com.goodee.beedan.dto.chatbot.ChatbotTopLevelTopicDto;
import com.goodee.beedan.entity.ChatMessage;
import com.goodee.beedan.entity.ChatRoom;
import com.goodee.beedan.entity.ChatRoomReadStatus;
import com.goodee.beedan.repository.chat.ChatMessageRepository;
import com.goodee.beedan.repository.chat.ChatRoomReadStatusRepository;
import com.goodee.beedan.repository.chat.ChatRoomRepository;
import com.goodee.beedan.service.chatbot.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberChatService {
    private static final String DEFAULT_CHAT_ROOM_TITLE = "기타 문의";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomReadStatusRepository chatRoomReadStatusRepository;
    private final ChatbotService chatbotService;
    private final ChatRealtimeService chatRealtimeService;

    // 챗봇 상담 연결 시 최상위 1차 질의명을 제목으로 사용해 채팅방 생성 또는 기존 활성방 반환
    public ChatRoomOpenResultDto openChatRoomFromChatbot(Long topicId, Long memId) {
        ChatbotTopLevelTopicDto topLevelTopic = chatbotService.getTopLevelTopic(topicId);
        String roomTitle = topLevelTopic != null ? topLevelTopic.getCbTpNm() : DEFAULT_CHAT_ROOM_TITLE;

        return openChatRoom(memId, roomTitle);
    }

    // 새 문의하기에서 기타 문의 제목으로 채팅방 생성 또는 기존 활성방 반환
    public ChatRoomOpenResultDto openNewInquiryChatRoom(Long memId) {
        return openChatRoom(memId, DEFAULT_CHAT_ROOM_TITLE);
    }

    // 제목만 받아 기존 활성방 반환 또는 새 OPEN 채팅방 생성
    private ChatRoomOpenResultDto openChatRoom(Long memId, String roomTitle) {
        List<ChatRoomStatus> activeStatuses = List.of(ChatRoomStatus.OPEN, ChatRoomStatus.ONGOING);

        // 활성 채팅방 조회
        ChatRoom activeRoom = chatRoomRepository
                .findFirstByMemIdAndChRoSttInOrderByChRoIdDesc(memId, activeStatuses)
                .orElse(null);

        if (activeRoom != null) {
            return mapToChatRoomOpenResultDto(activeRoom, true);
        }

        ChatRoom newRoom = ChatRoom.builder()
                .chRoTtl(roomTitle)
                .chRoStt(ChatRoomStatus.OPEN)
                .memId(memId)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);

        ChatRoomReadStatus roomReadStatus = ChatRoomReadStatus.builder()
                .memId(memId)
                .chRoId(savedRoom.getChRoId())
                .chRoReStUnrYn(false)
                .chMsLastId(null)
                .build();

        chatRoomReadStatusRepository.save(roomReadStatus);

        return mapToChatRoomOpenResultDto(savedRoom, false);
    }

    // 생성 또는 기존 반환 결과를 채팅방 오픈 응답 DTO로 변환
    private ChatRoomOpenResultDto mapToChatRoomOpenResultDto(ChatRoom chatRoom, boolean existingRoom) {
        return ChatRoomOpenResultDto.builder()
                .chRoId(chatRoom.getChRoId())
                .chRoTtl(chatRoom.getChRoTtl())
                .chRoStt(chatRoom.getChRoStt())
                .existingRoom(existingRoom)
                .build();
    }

    // 회원 본인의 채팅방 목록을 조회, 마지막 메시지와 미읽음 여부를 함께 반환
    public List<MemberChatRoomListDto> getMemberChatRooms(Long memId) {
        return chatRoomRepository.findMemberChatRoomsByMemIdOrderByActiveFirst(memId)
                .stream()
                .map(chatRoom -> mapToMemberChatRoomListDto(chatRoom, memId))
                .toList();
    }

    // 채팅방 기본 정보와 마지막 메시지, 읽음 상태를 조합해 사용자 채팅 목록 DTO로 변환
    private MemberChatRoomListDto mapToMemberChatRoomListDto(ChatRoom chatRoom, Long memId) {
        // 가장 최신 메시지 조회
        Optional<ChatMessage> lastMessage = chatMessageRepository
                .findFirstByChRoIdOrderByChMsCreDtDesc(chatRoom.getChRoId());

        // 사용자 기준 읽음 상태 조회
        Optional<ChatRoomReadStatus> roomReadStatus = chatRoomReadStatusRepository
                .findByMemIdAndChRoId(memId, chatRoom.getChRoId());

        return MemberChatRoomListDto.builder()
                .chRoId(chatRoom.getChRoId())
                .chRoTtl(chatRoom.getChRoTtl())
                .chRoStt(chatRoom.getChRoStt())
                .lastMessageContent(lastMessage.map(chatMessage -> chatMessage.getChMsCon()).orElse(null))
                .lastMessageCreatedAt(lastMessage.map(chatMessage -> chatMessage.getChMsCreDt()).orElse(null))
                .chRoCreDt(chatRoom.getChRoCreDt())
                .unread(roomReadStatus.map(chatRoomReadStatus -> chatRoomReadStatus.getChRoReStUnrYn()).orElse(false))
                .build();
    }

    // 회원 본인 채팅방 상세 조회와 마지막 메시지 기준 읽음 상태 갱신
    public MemberChatRoomDetailDto getMemberChatRoomDetail(Long chRoId, Long memId) {
        // 본인 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository
                .findByChRoIdAndMemId(chRoId, memId)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 채팅방입니다."));

        // 메시지 엔티티 목록 조회
        List<ChatMessage> messages = chatMessageRepository.findByChRoIdOrderByChMsCreDtAsc(chRoId);

        // 마지막 메시지 기준 읽음 상태 갱신
        updateChatRoomReadStatus(memId, chRoId, messages);

        List<MemberChatMessageDto> messageDtos = messages.stream()
                .map(this::mapToMemberChatMessageDto).toList();

        return mapToMemberChatRoomDetailDto(chatRoom, messageDtos);
    }

    // 상세 화면 진입 시 읽음 상태 row 생성 또는 마지막 메시지 기준으로 읽음 처리
    private void updateChatRoomReadStatus(Long memId, Long chRoId, List<ChatMessage> messages) {
        if (messages.isEmpty()) return;
        // 마지막 메시지 조회
        ChatMessage lastMessage = messages.get(messages.size() - 1);
        // 읽음 상태 조회 또는 생성
        ChatRoomReadStatus roomReadStatus = chatRoomReadStatusRepository
                .findByMemIdAndChRoId(memId, chRoId)
                .orElseGet(() -> ChatRoomReadStatus.builder()
                                                .memId(memId)
                                                .chRoId(chRoId)
                                                .build());

        roomReadStatus.setChMsLastId(lastMessage.getChMsId()); // 마지막 메시지 갱신
        roomReadStatus.setChRoReStUnrYn(false); // 미읽음 여부 FALSE -> 읽음

        chatRoomReadStatusRepository.save(roomReadStatus);
    }

    // 채팅 메시지 엔티티를 상세 화면용 메시지 DTO로 변환
    private MemberChatMessageDto mapToMemberChatMessageDto(ChatMessage chatMessage) {
        return MemberChatMessageDto.builder()
                .chMsId(chatMessage.getChMsId())
                .chMsSenTy(chatMessage.getChMsSenTy())
                .chMsCon(chatMessage.getChMsCon())
                .chMsCreDt(chatMessage.getChMsCreDt())
                .build();
    }

    // 채팅방 정보와 메시지 목록을 상세 DTO로 변환
    private MemberChatRoomDetailDto mapToMemberChatRoomDetailDto(ChatRoom chatRoom, List<MemberChatMessageDto> messageDtos) {
        return MemberChatRoomDetailDto.builder()
                .chRoId(chatRoom.getChRoId())
                .chRoTtl(chatRoom.getChRoTtl())
                .chRoStt(chatRoom.getChRoStt())
                .messages(messageDtos)
                .build();
    }

    // 회원이 메시지를 전송하고 읽음 상태 갱신
    public MemberChatMessageDto sendMemberChatMessage(Long chRoId, Long memId, String content) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository
                .findByChRoIdAndMemId(chRoId, memId)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 채팅방입니다."));

        // 종료 여부 검증
        if (chatRoom.getChRoStt() == ChatRoomStatus.CLOSED) {
            throw new IllegalArgumentException("종료된 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        // 내용 존재 검증
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메시지 내용을 입력해 주세요.");
        }

        // 메시지 엔티티 객체 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .chMsSenTy(ChatMessageSenderType.USER)
                .chMsCon(content.trim())
                .chRoId(chRoId)
                .memId(memId)
                .build();

        // 메시지 저장
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 채팅방 마지막 메시지 시각 갱신
        chatRoom.setChRoLastMsDt(savedMessage.getChMsCreDt());
        chatRoomRepository.save(chatRoom);

        // 사용자 읽음 상태 갱신
        ChatRoomReadStatus memberReadStatus = chatRoomReadStatusRepository
                .findByMemIdAndChRoId(memId, chRoId)
                .orElseGet(() -> ChatRoomReadStatus.builder()
                        .memId(memId)
                        .chRoId(chRoId)
                        .build());

        memberReadStatus.setChRoReStUnrYn(false); // 미읽음 여부 FALSE -> 읽음
        memberReadStatus.setChMsLastId(savedMessage.getChMsId()); // 마지막 메시지 갱신
        chatRoomReadStatusRepository.save(memberReadStatus);

        // ONGOING 방이면 담당자 미읽음 상태 갱신 (OPEN 방이면 담당자X, 읽음 상태X 이므로)
        if (chatRoom.getChRoStt() == ChatRoomStatus.ONGOING && chatRoom.getMemAdId() != null) {
            // 관리자 읽음 상태 갱신
            ChatRoomReadStatus adminReadStatus = chatRoomReadStatusRepository
                    .findByMemIdAndChRoId(chatRoom.getMemAdId(), chRoId)
                    .orElseGet(() -> ChatRoomReadStatus.builder()
                            .memId(chatRoom.getMemAdId())
                            .chRoId(chRoId)
                            .build());

            adminReadStatus.setChRoReStUnrYn(true); // 미읽음 여부 TRUE -> 안읽음
            adminReadStatus.setChMsLastId(savedMessage.getChMsId()); // 마지막 메시지 갱신
            chatRoomReadStatusRepository.save(adminReadStatus);
        }

        // 엔티티 -> Dto 변환
        MemberChatMessageDto memberChatMessageDto = mapToMemberChatMessageDto(savedMessage);
        // 채팅방 구독자들에게 실시간으로 메시지를 뿌림
        chatRealtimeService.publishMessage(chRoId, memberChatMessageDto);

        return memberChatMessageDto;
    }

    // 회원 본인 채팅방을 사용자 종료 상태로 변경
    public void closeMemberChatRoom(Long chRoId, Long memId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository
                .findByChRoIdAndMemId(chRoId, memId)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 채팅방입니다."));

        // 이미 종료된 채팅방인지 검증
        if (chatRoom.getChRoStt() == ChatRoomStatus.CLOSED) {
            throw new IllegalArgumentException("이미 종료된 채팅방입니다.");
        }

        chatRoom.setChRoStt(ChatRoomStatus.CLOSED);
        chatRoom.setChRoClsRsn(ChatRoomCloseReason.USER);
        chatRoom.setChRoClsDt(LocalDateTime.now());

        // 채팅방 저장
        chatRoomRepository.save(chatRoom);
    }
}
