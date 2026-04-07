package com.goodee.beedan.dto.board.notice;

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
public class NoticeBoardRequestDto {
    private Long brdId; // 수정 시 사용
    private String brdTtl;
    private String brdCon;
    private Boolean brdFixYn;
    private List<MultipartFile> newFiles; // 신규 첨부 파일
    private List<String> deleteUuids;     // 삭제할 파일 PK 리스트
}
