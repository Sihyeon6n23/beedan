package com.goodee.beedan.service.chat;

import com.goodee.beedan.common.constant.*;
import com.goodee.beedan.dto.chat.*;
import com.goodee.beedan.dto.file.FileDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.entity.ChatMessage;
import com.goodee.beedan.entity.ChatRoom;
import com.goodee.beedan.entity.ChatRoomReadStatus;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.repository.chat.ChatMessageRepository;
import com.goodee.beedan.repository.chat.ChatRoomReadStatusRepository;
import com.goodee.beedan.repository.chat.ChatRoomRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomReadStatusRepository chatRoomReadStatusRepository;
    private final ChatRealtimeService chatRealtimeService;
    private final FileService fileService;

    // 관리자 채팅 목록 조회 (페이징 + 상태 필터링 + 담당 필터링)
    public Page<AdminChatRoomListDto> getAdminChatRooms(AdminChatRoomSearchDto searchDto, Long memAdId) {
        validateAdminAuthority(memAdId);

        // 현재 페이지 번호와 페이지당 개수로 페이지 조회 조건 생성
        Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize());
        Page<ChatRoom> chatRoomPage;

        boolean myAssignedOnly = Boolean.TRUE.equals(searchDto.getMyAssignedOnly());
        boolean allStatus = "ALL".equals(searchDto.getStatus()); // 상태 필터가 전체인지

        String keyword = searchDto.getKeyword() == null ? "" : searchDto.getKeyword().trim();
        boolean hasKeyword = !keyword.isEmpty();

        if (hasKeyword) { // 검색어가 있으면 검색 전용 쿼리 사용
            chatRoomPage = chatRoomRepository.searchAdminChatRooms(
                    memAdId,
                    searchDto.getStatus(),
                    myAssignedOnly,
                    keyword,
                    pageable
            );
        } else if (allStatus && !myAssignedOnly) { // 전체 상태 + 전체 목록
            chatRoomPage = chatRoomRepository
                    .findAllByPriorityOrder(pageable);
        } else if (!allStatus && !myAssignedOnly) { // 특정 상태 + 전체 목록
            ChatRoomStatus status = ChatRoomStatus.valueOf(searchDto.getStatus()); // enum으로 변환
            chatRoomPage = chatRoomRepository
                    .findByChRoSttOrderByChRoLastMsDtDescChRoCreDtDesc(status, pageable);
        } else if (allStatus) { // 전체 상태 + 내 담당 목록
            chatRoomPage = chatRoomRepository
                    .findByMemAdIdPriorityOrder(memAdId, pageable);
        } else { // 특정 상태 + 내 담당 목록
            ChatRoomStatus status = ChatRoomStatus.valueOf(searchDto.getStatus()); // enum으로 변환
            chatRoomPage = chatRoomRepository
                    .findByChRoSttAndMemAdIdOrderByChRoLastMsDtDescChRoCreDtDesc(status, memAdId, pageable);
        }

        // 현재 페이지의 채팅방 목록 조회
        List<ChatRoom> chatRooms = chatRoomPage.getContent();

        // 목록에 포함된 회원/관리자/읽음 상태를 먼저 모아서 한 번에 조회
        // 중복 제거한 회원 id 목록
        List<Long> memberIds = chatRooms.stream()
                .map(ChatRoom::getMemId)
                .distinct()
                .toList();
        // 중복 제거, 담당자가 없는 null값 제거한 관리자 id 목록
        List<Long> memAdIds = chatRooms.stream()
                .map(ChatRoom::getMemAdId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // 채팅방 id 목록
        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoom::getChRoId)
                .toList();

        // 비어 있지 않으면 memberIds에 해당하는 회원들을 한 번에 조회 후 map(<memId, Member>)로 변환
        Map<Long, Member> memberMap = memberIds.isEmpty()
                ? Collections.emptyMap()
                : memberRepository.findByMemIdIn(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemId, member -> member));

        // 비어 있지 않으면 memAdIds에 해당하는 관리자들을 한 번에 조회 후 map(<memId, Member>)로 변환
        Map<Long, Member> adminMap = memAdIds.isEmpty()
                ? Collections.emptyMap()
                : memberRepository.findByMemIdIn(memAdIds).stream()
                .collect(Collectors.toMap(Member::getMemId, member -> member));

        // 비어 있지 않으면 현재 관리자의 읽음 상태를 현재 페이지 채팅방 id들에 대해서 한 번에 조회 후 map(<chRoId, ChatRoomReadStatus>)로 변환
        Map<Long, ChatRoomReadStatus> adminReadStatusMap = chatRoomIds.isEmpty()
                ? Collections.emptyMap()
                : chatRoomReadStatusRepository.findByMemIdAndChRoIdIn(memAdId, chatRoomIds).stream()
                .collect(Collectors.toMap(ChatRoomReadStatus::getChRoId, readStatus -> readStatus));

        // 미리 만들어 둔 memberMap, adminMap, adminReadStatusMap을 재사용 하여 최종 DTO 목록 생성
        Map<Long, ChatMessage> lastMessageMap = chatRoomIds.isEmpty()
                ? Collections.emptyMap()
                : chatMessageRepository.findLatestMessagesByChRoIds(chatRoomIds).stream()
                .collect(Collectors.toMap(ChatMessage::getChRoId, chatMessage -> chatMessage));

        List<AdminChatRoomListDto> contents = chatRooms.stream()
                .map(chatRoom -> mapToAdminChatRoomListDto(chatRoom, memberMap, adminMap, adminReadStatusMap, lastMessageMap))
                .toList();

        // DTO 목록을 다시 Page 형태로 감싸서 반환 (최종 DTO 목록, 페이지 번호/사이즈, 전체 개수)
        // Page 객체로 만드는 생성자
        return new PageImpl<>(contents, pageable, chatRoomPage.getTotalElements());
    }

    // 관리자 채팅 목록용 DTO 변환
    private AdminChatRoomListDto mapToAdminChatRoomListDto(
            ChatRoom chatRoom,
            Map<Long, Member> memberMap,
            Map<Long, Member> adminMap,
            Map<Long, ChatRoomReadStatus> adminReadStatusMap,
            Map<Long, ChatMessage> lastMessageMap
    ) {
        // 채팅방의 최근 메시지 1건 조회
        ChatMessage lastMessage = lastMessageMap.get(chatRoom.getChRoId());

        // 채팅방을 만든 사용자 정보 조회 -> 회사 상호명 조회
        Member member = memberMap.getOrDefault(chatRoom.getMemId(), new Member());

        // 담당 관리자 조회 -> 담당자명 조회
        Member admin = chatRoom.getMemAdId() != null
                ? adminMap.getOrDefault(chatRoom.getMemAdId(), new Member())
                : null;

        // 미읽음 상태 설정
        boolean unread = false;
        if (chatRoom.getChRoStt() == ChatRoomStatus.OPEN) { // 채팅방 상태가 OPEN 이면
            unread = true;
        } else if (chatRoom.getChRoStt() == ChatRoomStatus.ONGOING && chatRoom.getMemAdId() != null) { // 채팅방 상태가 ONGOING이고, 담당자가 있다면
            ChatRoomReadStatus readStatus = adminReadStatusMap.get(chatRoom.getChRoId());
            unread = readStatus != null && readStatus.getChRoReStUnrYn();
        }

        return AdminChatRoomListDto.builder()
                .chRoId(chatRoom.getChRoId())
                .memBizTtl(member.getMemBizTtl())
                .memNm(member.getMemNm())
                .lastMessageContent(lastMessage != null ? lastMessage.getChMsCon() : null)
                .lastMessageCreatedAt(lastMessage != null ? lastMessage.getChMsCreDt() : null)
                .chRoCreDt(chatRoom.getChRoCreDt())
                .chRoStt(chatRoom.getChRoStt())
                .unread(unread)
                .adminName(admin != null ? admin.getMemNm() : null)
                .chRoClsRsn(chatRoom.getChRoClsRsn())
                .build();
    }

    // 채팅방 시작 상태를 ONGOING으로 변경하고 담당자를 지정
    public void startAdminChatRoom(Long chRoId, Long memAdId) {
        validateAdminAuthority(memAdId);

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chRoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // OPEN 상태인지 검증
        if (chatRoom.getChRoStt() != ChatRoomStatus.OPEN) {
            throw new IllegalStateException("상담 시작은 OPEN 상태에서만 가능합니다.");
        }

        chatRoom.setMemAdId(memAdId); // 담당자 지정
        chatRoom.setChRoStt(ChatRoomStatus.ONGOING); // ONGOING 상태로 변경
        chatRoom.setChRoAsgDt(LocalDateTime.now()); // 상담 시작(배정) 시각 저장

        chatRoomRepository.save(chatRoom);

        // 마지막 메시지 조회
        Optional<ChatMessage> lastMessage = chatMessageRepository
                .findFirstByChRoIdOrderByChMsCreDtDesc(chRoId);

        ChatRoomReadStatus adminReadStatus = ChatRoomReadStatus.builder()
                .memId(memAdId)
                .chRoId(chRoId)
                .chRoReStUnrYn(false)
                .chMsLastId(lastMessage.map(chatMessage -> chatMessage.getChMsId()).orElse(null))
                .build();

        chatRoomReadStatusRepository.save(adminReadStatus);
    }

    // 담당자 본인이 채팅방을 종료 처리
    public void closeAdminChatRoom(Long chRoId, Long memAdId) {
        validateAdminAuthority(memAdId);

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chRoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 이미 종료된 채팅방인지 검증
        if (chatRoom.getChRoStt() == ChatRoomStatus.CLOSED) {
            throw new IllegalStateException("이미 종료된 채팅방입니다.");
        }

        // 담당자 미배정 방은 종료 불가
        if (chatRoom.getMemAdId() == null) {
            throw new IllegalStateException("담당자가 배정되지 않은 채팅방은 종료할 수 없습니다.");
        }

        // 담당자 본인만 종료 가능
        if (!chatRoom.getMemAdId().equals(memAdId)) {
            throw new IllegalStateException("담당자 본인만 상담을 종료할 수 있습니다.");
        }

        chatRoom.setChRoStt(ChatRoomStatus.CLOSED); // CLOSED 상태로 변경
        chatRoom.setChRoClsRsn(ChatRoomCloseReason.ADMIN); // 종료 사유 ADMIN
        chatRoom.setChRoClsDt(LocalDateTime.now()); // 상담 종료 시각 저장

        chatRoomRepository.save(chatRoom);
        // 채팅방 종료 실시간 반영 (사용자/관리자)
        chatRealtimeService.publishMemberSummary(chatRoom.getMemId());
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

    // 담당자(관리자)가 메시지를 전송하고 읽음 상태 갱신
    public AdminChatMessageDto sendAdminChatMessage(Long chRoId, Long memAdId, String content) {
        validateAdminAuthority(memAdId);

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chRoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 종료된 채팅방에는 메시지 전송 불가
        if (chatRoom.getChRoStt() == ChatRoomStatus.CLOSED) {
            throw new IllegalStateException("종료된 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        // 담당자 미배정 방은 메시지 전송 불가
        if (chatRoom.getMemAdId() == null) {
            throw new IllegalStateException("담당자가 배정되지 않은 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        // 담당자 본인만 메시지 전송 가능
        if (!chatRoom.getMemAdId().equals(memAdId)) {
            throw new IllegalStateException("담당자 본인만 메시지를 보낼 수 있습니다.");
        }

        // 메시지 내용 검증
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메시지 내용을 입력해 주세요.");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .chMsSenTy(ChatMessageSenderType.ADMIN)
                .chMsTp(ChatMessageType.TEXT)
                .chMsCon(content.trim())
                .chRoId(chRoId)
                .memId(memAdId)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        chatRoom.setChRoLastMsDt(savedMessage.getChMsCreDt()); // 마지막 메시지 시각 갱신
        chatRoomRepository.save(chatRoom);

        // 관리자 읽음 상태 갱신
        ChatRoomReadStatus adminReadStatus = chatRoomReadStatusRepository
                .findByMemIdAndChRoId(memAdId, chRoId)
                .orElseGet(() -> ChatRoomReadStatus.builder()
                        .memId(memAdId)
                        .chRoId(chRoId)
                        .build());

        adminReadStatus.setChRoReStUnrYn(false); // 미읽음 여부 FALSE -> 읽음
        adminReadStatus.setChMsLastId(savedMessage.getChMsId()); // 마지막 메시지 갱신
        chatRoomReadStatusRepository.save(adminReadStatus);

        // 사용자 읽음 상태 갱신
        ChatRoomReadStatus memberReadStatus = chatRoomReadStatusRepository
                .findByMemIdAndChRoId(chatRoom.getMemId(), chRoId)
                .orElseGet(() -> ChatRoomReadStatus.builder()
                        .memId(chatRoom.getMemId())
                        .chRoId(chRoId)
                        .build());

        memberReadStatus.setChRoReStUnrYn(true); // 미읽음 여부 TRUE -> 안읽음
        memberReadStatus.setChMsLastId(savedMessage.getChMsId()); // 마지막 메시지 갱신
        chatRoomReadStatusRepository.save(memberReadStatus);

        // 엔티티 -> Dto 변환
        AdminChatMessageDto adminChatMessageDto = mapToAdminChatMessageDto(savedMessage);
        // 채팅방 구독자들에게 실시간으로 메시지를 뿌림
        chatRealtimeService.publishMessage(chRoId, adminChatMessageDto);

        return adminChatMessageDto;
    }

    // 관리자 채팅 상세 조회
    public AdminChatRoomDetailDto getAdminChatRoomDetail(Long chRoId, Long memAdId) {
        validateAdminAuthority(memAdId);

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chRoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 메시지 목록 조회
        List<ChatMessage> chatMessages = chatMessageRepository
                .findByChRoIdOrderByChMsCreDtAsc(chRoId);

        // 사용자 정보 조회
        Member member = memberRepository.findById(chatRoom.getMemId())
                .orElseGet(Member::new);

        // 담당자 정보 조회
        Member admin = null;
        if (chatRoom.getMemAdId() != null) {
            admin = memberRepository.findById(chatRoom.getMemAdId())
                    .orElseGet(Member::new);
        }

        // 진행중 상담방을 담당자 본인이 조회하면 관리자 미읽음 상태를 읽음으로 갱신
        if (chatRoom.getChRoStt() == ChatRoomStatus.ONGOING
                && chatRoom.getMemAdId() != null
                && chatRoom.getMemAdId().equals(memAdId)) {
            ChatRoomReadStatus chatRoomReadStatus = chatRoomReadStatusRepository
                    .findByMemIdAndChRoId(memAdId, chRoId)
                    .orElseGet(() -> ChatRoomReadStatus.builder()
                            .memId(memAdId)
                            .chRoId(chRoId)
                            .build());

            chatRoomReadStatus.setChRoReStUnrYn(false); // 미읽음 여부 FALSE -> 읽음
            if (!chatMessages.isEmpty()) {
                chatRoomReadStatus.setChMsLastId(chatMessages.getLast().getChMsId()); // 마지막 메시지 갱신
            }
            chatRoomReadStatusRepository.save(chatRoomReadStatus);
        }

        List<AdminChatMessageDto> messageDtos = chatMessages.stream()
                .map(this::mapToAdminChatMessageDto)
                .toList();

        return mapToAdminChatRoomDetailDto(chatRoom, member, admin, messageDtos, chatRoom.getMemAdId() != null && chatRoom.getMemAdId().equals(memAdId));
    }

    // 관리자 채팅 이미지 메시지 전송
    public AdminChatMessageDto sendAdminChatImage(Long chRoId,
                                                  Long memAdId,
                                                  AdminChatImageMessageSendDto adminChatImageMessageSendDto) throws IOException {
        validateAdminAuthority(memAdId);
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chRoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        if (chatRoom.getChRoStt() == ChatRoomStatus.CLOSED) {
            throw new IllegalStateException("종료된 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        if (chatRoom.getMemAdId() == null) {
            throw new IllegalStateException("담당자가 배정되지 않은 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        if (!chatRoom.getMemAdId().equals(memAdId)) {
            throw new IllegalStateException("담당자 본인만 메시지를 보낼 수 있습니다.");
        }

        validateChatImageFile(adminChatImageMessageSendDto.getImageFile());

        ChatMessage chatMessage = ChatMessage.builder()
                .chMsSenTy(ChatMessageSenderType.ADMIN)
                .chMsTp(ChatMessageType.IMAGE)
                .chMsCon(null)
                .chRoId(chRoId)
                .memId(memAdId)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        fileService.saveFile(
                List.of(adminChatImageMessageSendDto.getImageFile()),
                RefDto.builder()
                        .refTy("CHAT_MESSAGE")
                        .refNo(savedMessage.getChMsId())
                        .build()
        );

        chatRoom.setChRoLastMsDt(savedMessage.getChMsCreDt());
        chatRoomRepository.save(chatRoom);

        ChatRoomReadStatus adminReadStatus = chatRoomReadStatusRepository
                .findByMemIdAndChRoId(memAdId, chRoId)
                .orElseGet(() -> ChatRoomReadStatus.builder()
                        .memId(memAdId)
                        .chRoId(chRoId)
                        .build());

        adminReadStatus.setChRoReStUnrYn(false);
        adminReadStatus.setChMsLastId(savedMessage.getChMsId());
        chatRoomReadStatusRepository.save(adminReadStatus);

        ChatRoomReadStatus memberReadStatus = chatRoomReadStatusRepository
                .findByMemIdAndChRoId(chatRoom.getMemId(), chRoId)
                .orElseGet(() -> ChatRoomReadStatus.builder()
                        .memId(chatRoom.getMemId())
                        .chRoId(chRoId)
                        .build());

        memberReadStatus.setChRoReStUnrYn(true);
        memberReadStatus.setChMsLastId(savedMessage.getChMsId());
        chatRoomReadStatusRepository.save(memberReadStatus);

        AdminChatMessageDto adminChatMessageDto = mapToAdminChatMessageDto(savedMessage);
        chatRealtimeService.publishMessage(chRoId, adminChatMessageDto);
        chatRealtimeService.publishMemberSummary(chatRoom.getMemId());
        chatRealtimeService.publishAdminSummary();

        return adminChatMessageDto;
    }

    // 관리자 채팅 견적 메시지 전송
    public AdminChatMessageDto sendAdminQuoteCard(Long chRoId,
                                                  Long memAdId,
                                                  AdminChatQuoteCardMessageSendDto adminChatQuoteCardMessageSendDto) {
        validateAdminAuthority(memAdId);

        ChatRoom chatRoom = chatRoomRepository.findById(chRoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        if (chatRoom.getChRoStt() == ChatRoomStatus.CLOSED) {
            throw new IllegalStateException("종료된 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        if (chatRoom.getMemAdId() == null) {
            throw new IllegalStateException("담당자가 배정되지 않은 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        if (!chatRoom.getMemAdId().equals(memAdId)) {
            throw new IllegalStateException("담당자 본인만 메시지를 보낼 수 있습니다.");
        }

        if (adminChatQuoteCardMessageSendDto.getChMsLnkTtl() == null || adminChatQuoteCardMessageSendDto.getChMsLnkTtl().isBlank()) {
            throw new IllegalArgumentException("견적 카드 제목은 필수입니다.");
        }

        if (adminChatQuoteCardMessageSendDto.getChMsLnkUrl() == null || adminChatQuoteCardMessageSendDto.getChMsLnkUrl().isBlank()) {
            throw new IllegalArgumentException("견적 링크는 필수입니다.");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .chMsSenTy(ChatMessageSenderType.ADMIN)
                .chMsTp(ChatMessageType.QUOTE_CARD)
                .chMsCon(adminChatQuoteCardMessageSendDto.getChMsCon() != null
                            && !adminChatQuoteCardMessageSendDto.getChMsCon().isBlank()
                            ? adminChatQuoteCardMessageSendDto.getChMsCon().trim()
                            : null)
                .chMsLnkTtl(adminChatQuoteCardMessageSendDto.getChMsLnkTtl().trim())
                .chMsLnkUrl(adminChatQuoteCardMessageSendDto.getChMsLnkUrl().trim())
                .chRoId(chRoId)
                .memId(memAdId)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        chatRoom.setChRoLastMsDt(savedMessage.getChMsCreDt());
        chatRoomRepository.save(chatRoom);

        ChatRoomReadStatus adminReadStatus = chatRoomReadStatusRepository
                .findByMemIdAndChRoId(memAdId, chRoId)
                .orElseGet(() -> ChatRoomReadStatus.builder()
                        .memId(memAdId)
                        .chRoId(chRoId)
                        .build());

        adminReadStatus.setChRoReStUnrYn(false);
        adminReadStatus.setChMsLastId(savedMessage.getChMsId());
        chatRoomReadStatusRepository.save(adminReadStatus);

        ChatRoomReadStatus memberReadStatus = chatRoomReadStatusRepository
                .findByMemIdAndChRoId(chatRoom.getMemId(), chRoId)
                .orElseGet(() -> ChatRoomReadStatus.builder()
                        .memId(chatRoom.getMemId())
                        .chRoId(chRoId)
                        .build());

        memberReadStatus.setChRoReStUnrYn(true);
        memberReadStatus.setChMsLastId(savedMessage.getChMsId());
        chatRoomReadStatusRepository.save(memberReadStatus);

        AdminChatMessageDto adminChatMessageDto = mapToAdminChatMessageDto(savedMessage);
        chatRealtimeService.publishMessage(chRoId, adminChatMessageDto);
        chatRealtimeService.publishMemberSummary(chatRoom.getMemId());
        chatRealtimeService.publishAdminSummary();

        return adminChatMessageDto;
    }

    // 관리자 메시지 DTO 변환
    private AdminChatMessageDto mapToAdminChatMessageDto(ChatMessage chatMessage) {
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

        return AdminChatMessageDto.builder()
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

    // 관리자 채팅 상세 DTO 변환
    private AdminChatRoomDetailDto mapToAdminChatRoomDetailDto(
            ChatRoom chatRoom,
            Member member,
            Member admin,
            List<AdminChatMessageDto> messageDtos,
            boolean canWrite
    ) {
        return AdminChatRoomDetailDto.builder()
                .chRoId(chatRoom.getChRoId())
                .memBizTtl(member.getMemBizTtl())
                .memNm(member.getMemNm())
                .adminName(admin != null ? admin.getMemNm() : null)
                .canWrite(canWrite)
                .chRoStt(chatRoom.getChRoStt())
                .chRoAsgDt(chatRoom.getChRoAsgDt())
                .chRoClsDt(chatRoom.getChRoClsDt())
                .chRoClsRsn(chatRoom.getChRoClsRsn())
                .messages(messageDtos)
                .build();
    }

    // 권한 검증
    private void validateAdminAuthority(Long adminId) {
        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));

        if (!admin.getMemAut().equals(MemberAuthority.ADMIN)
                && !admin.getMemAut().equals(MemberAuthority.ROOT)) {
            throw new IllegalArgumentException("관리자만 사용할 수 있는 기능입니다.");
        }
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
