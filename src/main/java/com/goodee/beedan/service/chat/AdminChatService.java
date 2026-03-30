package com.goodee.beedan.service.chat;

import com.goodee.beedan.common.constant.ChatRoomStatus;
import com.goodee.beedan.dto.chat.AdminChatRoomListDto;
import com.goodee.beedan.dto.chat.AdminChatRoomSearchDto;
import com.goodee.beedan.entity.ChatMessage;
import com.goodee.beedan.entity.ChatRoom;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.repository.chat.ChatMessageRepository;
import com.goodee.beedan.repository.chat.ChatRoomReadStatusRepository;
import com.goodee.beedan.repository.chat.ChatRoomRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomReadStatusRepository chatRoomReadStatusRepository;
    private final MemberRepository memberRepository;

    // 관리자 채팅 목록 조회 (페이징 + 상태 필터링 + 담당 필터링)
    public Page<AdminChatRoomListDto> getAdminChatRooms(AdminChatRoomSearchDto searchDto, Long adminId) {
        // 현재 페이지 번호와 페이지당 개수로 페이지 조회 조건 생성
        Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize());
        Page<ChatRoom> chatRoomPage;

        boolean myAssignedOnly = Boolean.TRUE.equals(searchDto.getMyAssignedOnly());
        boolean allStatus = "ALL".equals(searchDto.getStatus()); // 상태 필터가 전체인지

        if (allStatus && !myAssignedOnly) { // 전체 상태 + 전체 목록
            chatRoomPage = chatRoomRepository
                    .findAllByPriorityOrder(pageable);
        } else if (!allStatus && !myAssignedOnly) { // 특정 상태 + 전체 목록
            ChatRoomStatus status = ChatRoomStatus.valueOf(searchDto.getStatus()); // enum으로 변환
            chatRoomPage = chatRoomRepository
                            .findByChRoSttOrderByChRoLastMsDtDescChRoCreDtDesc(status, pageable);
        } else if (allStatus) { // 전체 상태 + 내 담당 목록
            chatRoomPage = chatRoomRepository
                    .findByMemAdIdPriorityOrder(adminId, pageable);
        } else { // 특정 상태 + 내 담당 목록
            ChatRoomStatus status = ChatRoomStatus.valueOf(searchDto.getStatus()); // enum으로 변환
            chatRoomPage = chatRoomRepository
                    .findByChRoSttAndMemAdIdOrderByChRoLastMsDtDescChRoCreDtDesc(status, adminId, pageable);
        }

        return chatRoomPage.map(this::mapToAdminChatRoomListDto);
    }

    private AdminChatRoomListDto mapToAdminChatRoomListDto(ChatRoom chatRoom) {
        // 채팅방의 최근 메시지 1개 조회
        Optional<ChatMessage> lastMessage = chatMessageRepository
                .findFirstByChRoIdOrderByChMsCreDtDesc(chatRoom.getChRoId());
        // 채팅방을 만든 사용자 정보 조회 -> 회사 상호명 조회
        Member member = memberRepository.findById(chatRoom.getMemId())
                .orElseThrow();
        // 관리자 정보 조회 -> 관리자 이름 조회
        Member admin = null;
        if (chatRoom.getMemAdId() != null) {
            admin = memberRepository.findById(chatRoom.getMemAdId())
                    .orElseThrow();
        }

        return AdminChatRoomListDto.builder()
                .chRoId(chatRoom.getChRoId())
                .memBizTtl(member.getMemBizTtl())
                .lastMessageContent(lastMessage.map(chatMessage -> chatMessage.getChMsCon()).orElse(null))
                .lastMessageCreatedAt(lastMessage.map(chatMessage -> chatMessage.getChMsCreDt()).orElse(null))
                .chRoCreDt(chatRoom.getChRoCreDt())
                .chRoStt(chatRoom.getChRoStt())
                .unread(false)
                .adminName(admin != null ? admin.getMemNm() : null)
                .build();
    }

    public void startAdminChatRoom(Long chRoId, Long adminId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository
                .findById(chRoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
        // OPEN 상태인지 검증
        if (chatRoom.getChRoStt() != ChatRoomStatus.OPEN) {
            throw new IllegalStateException("상담 시작은 OPEN 상태에서만 가능합니다.");
        }

        chatRoom.setMemAdId(adminId); // 담당자 지정
        chatRoom.setChRoStt(ChatRoomStatus.ONGOING); // ONGOING 상태로 변경
        chatRoom.setChRoAsgDt(LocalDateTime.now()); // 상담 시작 시각 저장

        chatRoomRepository.save(chatRoom);
    }
}
