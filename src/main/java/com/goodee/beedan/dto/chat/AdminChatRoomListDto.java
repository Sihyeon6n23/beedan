package com.goodee.beedan.dto.chat;

import com.goodee.beedan.common.constant.ChatRoomCloseReason;
import com.goodee.beedan.common.constant.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 관리자 채팅 목록 페이지에서 채팅방 1개을 출력하기 위한 DTO
public class AdminChatRoomListDto {
    private Long chRoId;
    private String memBizTtl;                       // 회사 상호명
    private String memNm;                           // 회원 이름
    private String lastMessageContent;              // 최근 메시지 내용
    private LocalDateTime lastMessageCreatedAt;     // 최근 메시지 시간
    private LocalDateTime chRoCreDt;                // 메시지 없을때 보조 시간
    private ChatRoomStatus chRoStt;                 // 상태 표시
    private Boolean unread;                         // 관리자 기준 미읽음 표시
    private String adminName;                       // 담당 관리자 이름

    private ChatRoomCloseReason chRoClsRsn;
}
