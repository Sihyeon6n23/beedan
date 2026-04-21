package com.goodee.beedan.dto.board.inquiry;

import com.goodee.beedan.dto.file.FileDto;
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
// 문의 답글용
public class InquiryReplyDto {
    private Long brdId;             // 답글 id
    private String brdCon;          // 답글 내용
    private String memAdName;       // 답글 작성자(관리자) 이름
    private LocalDateTime brdCreDt; // 답글 작성 일시
    private LocalDateTime brdUpdDt; // 답글 수정 일시
    private Boolean edited;         // 답글 수정 여부

    private List<FileDto> fileList;
}
