package com.goodee.beedan.service.chat;

import com.goodee.beedan.common.constant.ChatMessageSenderType;
import com.goodee.beedan.common.constant.ChatMessageType;
import com.goodee.beedan.common.constant.ChatRoomCloseReason;
import com.goodee.beedan.common.constant.ChatRoomStatus;
import com.goodee.beedan.dto.chat.*;
import com.goodee.beedan.dto.chatbot.ChatbotTopLevelTopicDto;
import com.goodee.beedan.dto.file.FileDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.entity.ChatMessage;
import com.goodee.beedan.entity.ChatRoom;
import com.goodee.beedan.entity.ChatRoomReadStatus;
import com.goodee.beedan.repository.chat.ChatMessageRepository;
import com.goodee.beedan.repository.chat.ChatRoomReadStatusRepository;
import com.goodee.beedan.repository.chat.ChatRoomRepository;
import com.goodee.beedan.service.chatbot.ChatbotService;
import com.goodee.beedan.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberChatService {
    private static final String DEFAULT_CHAT_ROOM_TITLE = "기타 문의";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomReadStatusRepository chatRoomReadStatusRepository;
    private final ChatbotService chatbotService;
    private final ChatRealtimeService chatRealtimeService;
    private final FileService fileService;

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

        LocalDateTime now = LocalDateTime.now();

        ChatRoom newRoom = ChatRoom.builder()
                .chRoTtl(roomTitle)
                .chRoStt(ChatRoomStatus.OPEN)
                .chRoLastMsDt(now)
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

        // 새 OPEN 채팅방 생성 즉시 사용자/관리자 목록에 반영
        chatRealtimeService.publishMemberSummary(memId);
        chatRealtimeService.publishAdminSummary();

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

    // 회원 본인 목록 화면에 필요한 값(방 정보, 마지막 메시지, 읽음 여부)을 한 번에 조회
    public List<MemberChatRoomListDto> getMemberChatRooms(Long memId) {
        return chatRoomRepository.findMemberChatRoomListSummaries(memId)
                .stream()
                .map(this::mapToMemberChatRoomListDto)
                .toList();
    }

    // 채팅방 기본 정보와 마지막 메시지, 읽음 상태를 조합해 사용자 채팅 목록 DTO로 변환
    private MemberChatRoomListDto mapToMemberChatRoomListDto(ChatRoomRepository.MemberChatRoomListProjection row) {
        // 마지막 메시지 타입에 따라 목록에서 보여줄 요약 문구를 결정
        String lastMessageSummary = getChatRoomListSummary(
                row.getLastMessageType(),
                row.getLastMessageContent()
        );

        return MemberChatRoomListDto.builder()
                .chRoId(row.getChRoId())
                .chRoTtl(row.getChRoTtl())
                // projection에서는 enum이 아니라 문자열로 오므로 valueOf로 변환
                .chRoStt(ChatRoomStatus.valueOf(row.getChRoStt()))
                .lastMessageContent(lastMessageSummary)
                .lastMessageCreatedAt(row.getLastMessageCreatedAt())
                .chRoCreDt(row.getChRoCreDt())
                // native query에서 unread를 정수(1/0)로 받아서 Boolean으로 변환
                .unread(row.getUnread() != null && row.getUnread() == 1)
                .chRoClsRsn(row.getChRoClsRsn() != null
                                ? ChatRoomCloseReason.valueOf(row.getChRoClsRsn())
                                : null
                )
                .build();
    }

    private String getChatRoomListSummary(String lastMessageType, String lastMessageContent) {
        if (lastMessageType == null) {
            return null;
        }

        // 이미지 메시지는 고정 문구로 표시
        if (ChatMessageType.IMAGE.name().equals(lastMessageType)) {
            return "이미지를 보냈습니다.";
        }

        // 견적 카드 메시지도 고정 문구로 표시
        if (ChatMessageType.QUOTE_CARD.name().equals(lastMessageType)) {
            return "견적을 보냈습니다.";
        }

        return lastMessageContent;
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
        ChatMessage lastMessage = messages.getLast();
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
                .chMsTp(ChatMessageType.TEXT)
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
        // 현재 방 메시지 실시간 반영
        chatRealtimeService.publishMessage(chRoId, memberChatMessageDto);
        // 사용자 본인 위젯 목록/배지 갱신
        chatRealtimeService.publishMemberSummary(memId);
        // 관리자 목록 갱신
        chatRealtimeService.publishAdminSummary();

        return memberChatMessageDto;
    }

    // 사용자 채팅 이미지 메시지 전송
    public MemberChatMessageDto sendMemberChatImage(Long chRoId,
                                                    Long memId,
                                                    MemberChatImageMessageSendDto memberChatImageMessageSendDto) throws IOException {
        ChatRoom chatRoom = chatRoomRepository
                .findByChRoIdAndMemId(chRoId, memId)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 채팅방입니다."));

        if (chatRoom.getChRoStt() == ChatRoomStatus.CLOSED) {
            throw new IllegalArgumentException("종료된 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        validateChatImageFile(memberChatImageMessageSendDto.getImageFile());

        ChatMessage chatMessage = ChatMessage.builder()
                .chMsSenTy(ChatMessageSenderType.USER)
                .chMsTp(ChatMessageType.IMAGE)
                .chMsCon(null)
                .chRoId(chRoId)
                .memId(memId)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        fileService.saveFile(
                List.of(memberChatImageMessageSendDto.getImageFile()),
                RefDto.builder()
                        .refTy("CHAT_MESSAGE")
                        .refNo(savedMessage.getChMsId())
                        .build()
        );

        chatRoom.setChRoLastMsDt(savedMessage.getChMsCreDt());
        chatRoomRepository.save(chatRoom);

        ChatRoomReadStatus memberReadStatus = chatRoomReadStatusRepository
                .findByMemIdAndChRoId(memId, chRoId)
                .orElseGet(() -> ChatRoomReadStatus.builder()
                        .memId(memId)
                        .chRoId(chRoId)
                        .build());

        memberReadStatus.setChRoReStUnrYn(false);
        memberReadStatus.setChMsLastId(savedMessage.getChMsId());
        chatRoomReadStatusRepository.save(memberReadStatus);

        if (chatRoom.getChRoStt() == ChatRoomStatus.ONGOING && chatRoom.getMemAdId() != null) {
            ChatRoomReadStatus adminReadStatus = chatRoomReadStatusRepository
                    .findByMemIdAndChRoId(chatRoom.getMemAdId(), chRoId)
                    .orElseGet(() -> ChatRoomReadStatus.builder()
                            .memId(chatRoom.getMemAdId())
                            .chRoId(chRoId)
                            .build());

            adminReadStatus.setChRoReStUnrYn(true);
            adminReadStatus.setChMsLastId(savedMessage.getChMsId());
            chatRoomReadStatusRepository.save(adminReadStatus);
        }

        MemberChatMessageDto memberChatMessageDto = mapToMemberChatMessageDto(savedMessage);
        chatRealtimeService.publishMessage(chRoId, memberChatMessageDto);
        chatRealtimeService.publishMemberSummary(memId);
        chatRealtimeService.publishAdminSummary();

        return memberChatMessageDto;
    }

    // 채팅방 정보와 메시지 목록을 상세 DTO로 변환
    private MemberChatMessageDto mapToMemberChatMessageDto(ChatMessage chatMessage) {
        FileDto imageFile = null;
        if (chatMessage.getChMsTp() == ChatMessageType.IMAGE) {
            imageFile = fileService.getFileList(
                            RefDto.builder()
                                    .refTy("CHAT_MESSAGE")
                                    .refNo(chatMessage.getChMsId())
                                    .build()
                    ).stream()
                    .findFirst()
                    .orElse(null);
        }

        return MemberChatMessageDto.builder()
                .chMsId(chatMessage.getChMsId())
                .chMsSenTy(chatMessage.getChMsSenTy())
                .chMsTp(chatMessage.getChMsTp())
                .chMsCon(chatMessage.getChMsCon())
                .chMsLnkUrl(chatMessage.getChMsLnkUrl())
                .chMsLnkTtl(chatMessage.getChMsLnkTtl())
                .chMsCreDt(chatMessage.getChMsCreDt())
                .imageFile(imageFile)
                .build();
    }

    // 채팅 메시지 엔티티를 상세 화면용 메시지 DTO로 변환
    private MemberChatRoomDetailDto mapToMemberChatRoomDetailDto(ChatRoom chatRoom, List<MemberChatMessageDto> messageDtos) {
        return MemberChatRoomDetailDto.builder()
                .chRoId(chatRoom.getChRoId())
                .chRoTtl(chatRoom.getChRoTtl())
                .chRoStt(chatRoom.getChRoStt())
                .chRoCreDt(chatRoom.getChRoCreDt())
                .messages(messageDtos)
                .chRoClsRsn(chatRoom.getChRoClsRsn())
                .build();
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
        // 채팅방 종료 실시간 반영 (사용자/관리자)
        chatRealtimeService.publishMemberSummary(memId);
        chatRealtimeService.publishAdminSummary();
        chatRealtimeService.publishRoomStatus(
                chRoId,
                ChatRoomStatusEventDto.builder()
                        .eventType("ROOM_STATUS")
                        .chRoId(chRoId)
                        .chRoStt(chatRoom.getChRoStt())
                        .chRoClsRsn(chatRoom.getChRoClsRsn())
                        .build()
        );
    }

    // 이미지 검증
    private void validateChatImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일은 필수입니다.");
        }

        String originalName = imageFile.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new IllegalArgumentException("올바르지 않은 이미지 파일명입니다.");
        }

        String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase().trim();
        List<String> allowedExts = List.of("jpg", "jpeg", "png", "webp");
        if (!allowedExts.contains(ext)) {
            throw new IllegalArgumentException("채팅 이미지는 jpg, jpeg, png, webp만 업로드할 수 있습니다.");
        }

        long maxSize = 5L * 1024 * 1024;
        if (imageFile.getSize() > maxSize) {
            throw new IllegalArgumentException("채팅 이미지는 5MB 이하만 업로드할 수 있습니다.");
        }
    }
}
