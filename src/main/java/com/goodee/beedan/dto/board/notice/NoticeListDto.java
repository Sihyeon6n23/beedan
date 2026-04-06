package com.goodee.beedan.dto.board.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeListDto {
    private Long brdId;          // 게시글 고유 ID
    private String brdTtl;       // 제목
    private String memNm;        // 작성자 이름 (Join 필요)
    private LocalDateTime brdCreDt; // 작성일
    private int brdHit;          // 조회수
    private int fileCnt;         // 첨부파일 개수 (Subquery 또는 Join)
}
