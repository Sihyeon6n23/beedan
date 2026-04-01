package com.goodee.beedan.dto.chat;

import com.goodee.beedan.common.constant.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 관리자 상세 화면의 채팅방 기본 정보 + 메시지 목록
public class AdminChatRoomDetailDto {
    private Long chRoId;
    private String memBizTtl;
    private String memNm;
    private String adminName;
    private Boolean canWrite;
    private ChatRoomStatus chRoStt;
    private LocalDateTime chRoAsgDt;
    private LocalDateTime chRoClsDt;
    private List<AdminChatMessageDto> messages;
}
