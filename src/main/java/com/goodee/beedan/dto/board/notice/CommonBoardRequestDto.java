package com.goodee.beedan.dto.board.notice;

import com.goodee.beedan.common.constant.InquiryStatus;
import com.goodee.beedan.dto.board.inquiry.InquiryReplyDto;
import com.goodee.beedan.dto.file.FileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonBoardRequestDto {
    // --- [식별자] 수정 시에는 필수, 등록 시에는 null ---
    private Long brdId;

    // --- [기본 정보] 모든 게시판 공통 ---
    private String brdTtl;
    private String brdCon;

    // --- [게시판별 선택 항목] ---
    private Boolean brdFixYn;         // 공지사항용
    private InquiryStatus brdInqStt;  // 문의게시판 상태 변경용 (관리자)

    // --- [파일 처리의 핵심] ---
    // MultipartFile은 JSON이 아닌 Multipart/form-data로 받아야 함
    private List<FileDto> existingFiles;
    private List<MultipartFile> newFiles; // 신규 첨부 파일
    private List<String> deleteUuids;     // 삭제할 기존 파일 PK(UUID) 리스트
}