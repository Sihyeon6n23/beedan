package com.goodee.beedan.dto.board.notice;

import com.goodee.beedan.dto.file.FileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDetailDto {
    // ... 기존 필드 (brdId, brdTtl, brdCon 등) ...

    private Long brdId;
    private String brdTtl;
    private String brdCon;
    private Long brdVstCnt;
    private Boolean brdFixYn;
    private LocalDateTime brdCreDt;
    private String memNm;
    private boolean canModify;
    private List<FileDto> fileList;

    private Long memId;

    // 추가: 이전/다음글 정보
    private NeighborNotice prevNotice;
    private NeighborNotice nextNotice;

    @Data
    @AllArgsConstructor
    public static class NeighborNotice {
        private Long id;
        private String title;
    }
}
