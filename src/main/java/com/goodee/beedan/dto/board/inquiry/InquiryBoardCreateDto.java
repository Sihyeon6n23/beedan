package com.goodee.beedan.dto.board.inquiry;

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
// 사용자 새 문의 작성용
public class InquiryBoardCreateDto {
    private String brdTtl;
    private String brdCon;

    private List<MultipartFile> newFiles; // 문의 작성 시 업로드한 신규 첨부파일 목록
}
