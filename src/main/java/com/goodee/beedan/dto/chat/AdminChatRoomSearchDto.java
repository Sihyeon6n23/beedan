package com.goodee.beedan.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 관리자 채팅 목록 조회 조건 + 페이지 정보 전달을 위한 DTO
public class AdminChatRoomSearchDto {
    private String status;
    private String keyword;
    private Boolean myAssignedOnly; // 내 담당 여부
    private int page;               // 현재 페이지 번호
    private int size;               // 페이지당 개수
}
